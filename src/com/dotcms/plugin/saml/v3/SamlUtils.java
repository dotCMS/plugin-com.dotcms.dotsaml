package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.security.RandomIdentifierGenerationStrategy;

import org.joda.time.DateTime;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.ArtifactResponse;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.DOT_SAML_DEFAULT_SERVICE_PROVIDER_PROTOCOL;
import static com.dotmarketing.util.UtilMethods.isSet;

/**
 * Provides utils method for the Saml
 * @author jsanca
 */
public class SamlUtils {

    private final static RandomIdentifierGenerationStrategy secureRandomIdGenerator =
            new RandomIdentifierGenerationStrategy();

    private final static XMLObjectBuilderFactory builderFactory =
            XMLObjectProviderRegistrySupport.getBuilderFactory();

    private final static MarshallerFactory marshallerFactory =
            XMLObjectProviderRegistrySupport.getMarshallerFactory();


    private static final String DEFAULT_ELEMENT_NAME = "DEFAULT_ELEMENT_NAME";


    private static Map<String, Credential> credentialMap = new ConcurrentHashMap<>();

    private static Map<String, Credential> idpCredentialMap = new ConcurrentHashMap<>();

    /**
     * Build a SAML Object
     * @param clazz
     * @param <T>
     * @return T
     */
    public static <T> T buildSAMLObject(final Class<T> clazz) {

        T object = null;
        QName defaultElementName = null;

        try {

            defaultElementName = (QName)clazz.getDeclaredField
                    (DEFAULT_ELEMENT_NAME).get(null);
            object = (T)builderFactory.getBuilder(defaultElementName)
                    .buildObject(defaultElementName);
        } catch (IllegalAccessException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new IllegalArgumentException("Could not create SAML object");
        } catch (NoSuchFieldException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new IllegalArgumentException("Could not create SAML object");
        }

        return object;
    } // buildSAMLObject.

    /**
     * Build an authentication request.
     * @return AuthnRequest
     */
    public static AuthnRequest buildAuthnRequest(final HttpServletRequest request, final Configuration configuration) {

        final String ipDSSODestination  = getIPDSSODestination(configuration);
        final AuthnRequest authnRequest = buildSAMLObject(AuthnRequest.class);

        // this ensure that the message redirected is not too old
        authnRequest.setIssueInstant(new DateTime());

        // IDP url
        if (!isSet(ipDSSODestination)) {

            Logger.error(SamlUtils.class, "The ipDSSODestination is not set in the idp metadata, neither the configuration files");
            throw new DotSamlException("The property: " + DotSamlConstants.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL +
                " must be set on the host");
        }

        authnRequest.setDestination(ipDSSODestination);

        // Get the protocol from the user, or use a default one: SAMLConstants.SAML2_ARTIFACT_BINDING_URI
        authnRequest.setProtocolBinding
                (configuration.getStringProperty(DotSamlConstants.DOTCMS_SAML_PROTOCOL_BINDING,
                        SAMLConstants.SAML2_ARTIFACT_BINDING_URI));

        // this is the address that receives the SAML Assertion, after a successful authentication on the IdP.
        authnRequest.setAssertionConsumerServiceURL(getAssertionConsumerEndpoint(request, configuration));

        // this is a uid or random id just to identified the response.
        authnRequest.setID(generateSecureRandomId());

        // id for the sender
        authnRequest.setIssuer(buildIssuer(configuration));

        authnRequest.setNameIDPolicy(buildNameIdPolicy(configuration));
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(configuration));
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setForceAuthn(configuration.getBooleanProperty
                (DotSamlConstants.DOTCMS_SAML_FORCE_AUTHN, false));

        return authnRequest;
    } // buildAuthnRequest.

    /**
     * Gets from the dotmarketing-config.properties the destination sso url
     * @return String
     */
    public static String getIPDSSODestination(final Configuration configuration) {

        final String redirectIdentityProviderDestinationSSOURL =
                    configuration.getIdentityProviderDestinationSSOURL(configuration);

        // first check the meta data info., secondly the configuration
        return (null != redirectIdentityProviderDestinationSSOURL)?
                redirectIdentityProviderDestinationSSOURL:
                configuration.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL, null);
    } // getIPDSSODestination.

    public static  String getAssertionConsumerEndpoint(final HttpServletRequest request, final Configuration configuration) {

        final String assertionConsumerEndpoint =
                configuration.getAssertionConsumerEndpoint();

        // this is the same original request. Consequently where should be redirected when the authentication is done.
        final StringBuilder builder = new StringBuilder(request.getRequestURI());

        if (null != request.getQueryString()) {

            builder.append('?')
                    .append(request.getQueryString());
        }

        return (null != assertionConsumerEndpoint)?
                assertionConsumerEndpoint:
                builder.toString();
    } // getAssertionConsumerEndpoint.

    /**
     * Generate the Random id
     * @return String
     */
    public static String generateSecureRandomId() {

        return secureRandomIdGenerator.generateIdentifier();
    } // generateSecureRandomId.

    /**
     * Build the Id for the sender.
     * @return Issuer
     */
    public static Issuer buildIssuer(final Configuration configuration) {

        final Issuer issuer =
                buildSAMLObject(Issuer.class);
        issuer.setValue(getSPIssuerValue(configuration));

        return issuer;
    } // buildIssuer.

    /**
     * Get the od for the Issuer, it is the SP identifier on the IdP
     * @return String
     */
    public static String getSPIssuerValue(final Configuration configuration) {

        final String defaultHost = configuration.getSiteName();

        return configuration.getStringProperty(
            DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER,
            getSiteName(configuration));
    } // getSPIssuerValue.

    private static String getSiteName (final Configuration configuration) {

        final String defaultHost = configuration.getSiteName();

        return UtilMethods.isSet (defaultHost) ? defaultHost :
                configuration.getStringProperty(DOT_SAML_DEFAULT_SERVICE_PROVIDER_PROTOCOL, null) + "://"
                    + SPIIssuerResolver.getDefaultServiceProviderIssuer().getHostname();
    } // getSiteName.

    /**
     * Return the policy for the Name ID (which is the IdP identifier for the user)
     * @return NameIDPolicy
     */
    public static NameIDPolicy buildNameIdPolicy(final Configuration configuration) {

        final NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);

        // True if you want that when the  user does not exists, allows to create
        nameIDPolicy.setAllowCreate(configuration.getBooleanProperty(DotSamlConstants.DOTCMS_SAML_POLICY_ALLOW_CREATE, false));
        // todo: should set the SPNameQualifier
        // it supports several formats, such as Kerberos, email, Windows Domain Qualified Name, etc.
        nameIDPolicy.setFormat(configuration.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_POLICY_FORMAT,
                // “The transient identifier is a random identifier that does not have any connection to the user. A transient identifier will be different for every time the user signs in.”
                NameIDType.PERSISTENT));

        return nameIDPolicy;
    } // buildNameIdPolicy.
    /**
     * if(Properties.getBoolean(Constants.ADD_AUTHN_POLICYNAME,true)){
     final NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
     NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
     nameIdPolicy.setFormat(Properties.getString(Constants.POLICYNAME_FORMAT,Constants.NAMEID_TRASIENT_FORMAT));
     nameIdPolicy.setSPNameQualifier(Properties.getString(Constants.POLICYNAME_SPNAMEQUALIFIER,Constants.ISSUER));
     nameIdPolicy.setAllowCreate(true);
     authRequest.setNameIDPolicy(nameIdPolicy);
     }
     */

    // todo: keep in mind more than one authentication can be defined

    /**
     * Build the Authentication context, with the login and password strategies
     * @return RequestedAuthnContext
     */
    public static RequestedAuthnContext buildRequestedAuthnContext(final Configuration configuration) {

        final RequestedAuthnContext requestedAuthnContext =
                buildSAMLObject(RequestedAuthnContext.class);

        requestedAuthnContext.setComparison
                (getAuthnContextComparisonTypeEnumeration(configuration));

        final AuthnContextClassRef passwordAuthnContextClassRef =
                buildSAMLObject(AuthnContextClassRef.class);

        passwordAuthnContextClassRef.setAuthnContextClassRef
                (configuration.getStringProperty(DotSamlConstants.DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF,
                        AuthnContext.PASSWORD_AUTHN_CTX));

        requestedAuthnContext.getAuthnContextClassRefs().
                add(passwordAuthnContextClassRef);

        return requestedAuthnContext;
    } // buildRequestedAuthnContext.

    /**
     * Based on the configuration properties get the desire comparison type
     * @return AuthnContextComparisonTypeEnumeration
     */
    public static AuthnContextComparisonTypeEnumeration getAuthnContextComparisonTypeEnumeration(final Configuration configuration) {

        AuthnContextComparisonTypeEnumeration comparisonTypeEnumeration =
                AuthnContextComparisonTypeEnumeration.MINIMUM;

        final String enumName = configuration.getStringProperty
                (DotSamlConstants.DOTCMS_SAML_AUTHN_COMPARISON_TYPE, null);

        if (isSet(enumName)) {

            if (AuthnContextComparisonTypeEnumeration.BETTER.toString().equalsIgnoreCase(enumName)) {

                comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.BETTER;
            } else if (AuthnContextComparisonTypeEnumeration.EXACT.toString().equalsIgnoreCase(enumName)) {

                comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.EXACT;
            } else if (AuthnContextComparisonTypeEnumeration.MAXIMUM.toString().equalsIgnoreCase(enumName)) {

                comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.MAXIMUM;
            } // MINIMUN is not necessary since it is the default one.
        }

        return comparisonTypeEnumeration;
    } // getAuthnContextComparisonTypeEnumeration.


    /**
     * Get the Identity Provider Destination
     * @return Endpoint
     */
    public static Endpoint getIdentityProviderDestinationEndpoint(final Configuration configuration) {

        final SingleSignOnService endpoint = buildSAMLObject(SingleSignOnService.class);

        endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        endpoint.setLocation(getIPDSSODestination(configuration));

        return endpoint;
    } // getIdentityProviderDestinationEndpoint.

    /**
     * Get the Assertion decrypted
     * @param artifactResponse {@link ArtifactResponse}
     * @return Assertion
     */
    public static Assertion getAssertion(final ArtifactResponse artifactResponse,
                                         final Configuration configuration) {

        final EncryptedAssertion encryptedAssertion = getEncryptedAssertion(artifactResponse);
        final Assertion assertion = decryptAssertion(encryptedAssertion, configuration); /// this is the user message itself

        return assertion;
    } // getAssertion.

    /**
     * Get the Assertion decrypted
     * @param response {@link Response}
     * @return Assertion
     */
    public static Assertion getAssertion(final Response response,
                                         final Configuration configuration) {

        final EncryptedAssertion encryptedAssertion;
        Assertion assertion = null;

        if (configuration.getBooleanProperty(DotSamlConstants.DOTCMS_SAML_IS_ASSERTION_ENCRYPTED, true)) {

            encryptedAssertion = response.getEncryptedAssertions().get(0);
            assertion = decryptAssertion(encryptedAssertion, configuration); /// this is the user message itself
        } else {

            assertion = response.getAssertions().get(0);
        }

        return assertion;
    } // getAssertion.

    /**
     * Just get the Encrypted assertion from the {@link ArtifactResponse}
     * @param artifactResponse {@link ArtifactResponse}
     * @return EncryptedAssertion
     */
    public static EncryptedAssertion getEncryptedAssertion(final ArtifactResponse artifactResponse) {

        final Response response = (Response)artifactResponse.getMessage();
        return response.getEncryptedAssertions().get(0);
    } // getEncryptedAssertion.

    /**
     * Decrypt an {@link EncryptedAssertion}
     * @param encryptedAssertion {@link EncryptedAssertion}
     * @return Assertion
     */
    public static Assertion decryptAssertion(final EncryptedAssertion encryptedAssertion,
                                             final Configuration configuration) {

        Assertion assertion = null;
        final StaticKeyInfoCredentialResolver keyInfoCredentialResolver =
                new StaticKeyInfoCredentialResolver(getCredential(configuration));

        final Decrypter decrypter = new Decrypter(null,
                keyInfoCredentialResolver, new InlineEncryptedKeyResolver());

        try {

            decrypter.setRootInNewDocument(true);
            assertion = decrypter.decrypt(encryptedAssertion);
        } catch (DecryptionException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        return assertion;
    } // decryptAssertion.

    private static void validateSignature(final Assertion assertion,
                                          final Collection<Credential> credentials) throws SignatureException {

        for (Credential credential : credentials) {
            try {

                SignatureValidator.validate(assertion.getSignature(), credential);
                return;
            } catch (SignatureException ignore) {

                Logger.info(SamlUtils.class, "Validation failed with credential: " + ignore.getMessage());
                //Logger.error(SamlUtils.class, "Validation failed with credential", ignore);
            }
        }

        throw new SignatureException("Signature cannot be validated");
    } // validateSignature.

    /**
     * Does the verification of the assertiong
     * @param assertion {@link Assertion}
     */
    public static void verifyAssertionSignature(final Assertion assertion,
                                                final Configuration configuration) {

        final SAMLSignatureProfileValidator profileValidator;

        if (configuration.getBooleanProperty(DotSamlConstants.DOTCMS_SAML_CHECKIF_ASSERTION_SIGNED, true) && !assertion.isSigned()) {

            Logger.error(SamlUtils.class, "The assertion is not signed...");
            throw new DotSamlException("The SAML Assertion was not signed");
        }

        try {

            if (configuration.isVerifySignatureProfileNeeded()) {

                Logger.debug(SamlUtils.class, "Doing Profile Validation");
                profileValidator = new SAMLSignatureProfileValidator();
                profileValidator.validate(assertion.getSignature());
                Logger.debug(SamlUtils.class, "Done Profile Validation");
            } else {

                Logger.info(SamlUtils.class, "Skipping the Verify Signature Profile check");
            }

            // Ask on the config if the app wants signature validator
            if (configuration.isVerifySignatureCredentialsNeeded()) {

                if (null != configuration.getSigningCredentials ()) {

                    Logger.debug(SamlUtils.class, "Validating the signatures: " + configuration.getSigningCredentials ());
                    validateSignature(assertion, configuration.getSigningCredentials ());
                    Logger.debug(SamlUtils.class, "Doing signatures validation");
                } else {

                    Logger.debug(SamlUtils.class, "Validating the signature with a IdP Credentials " );
                    SignatureValidator.validate(assertion.getSignature(), getIdPCredentials(configuration));
                    Logger.debug(SamlUtils.class, "Done validation of the signature with a IdP Credentials " );
                }
            } else {

                Logger.info(SamlUtils.class, "Skipping the Verify Signature Profile check");
            }

            Logger.info(SamlUtils.class, "SAML Assertion signature verified");

        } catch (SignatureException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // verifyAssertionSignature.

    /**
     * Read from the key store using a given password
     * @param pathToKeyStore {@link String}
     * @param keyStorePassword {@link String}
     * @return KeyStore
     */
    public static KeyStore readKeyStoreFromFile(final String pathToKeyStore,
                                                final String keyStorePassword,
                                                final String keyStoreType) {

        final KeyStore keystore;
        InputStream inputStream = null;

        try {
            keystore = KeyStore.getInstance(keyStoreType);
            inputStream = InputStreamUtils.getInputStream(pathToKeyStore);
            keystore.load(inputStream, keyStorePassword.toCharArray());
        } catch (Exception e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException("Something went wrong reading keystore", e);
        } finally {

            IOUtils.closeQuietly(inputStream);
        }

        return keystore;
    } // readKeystoreFromFile.

    public static Credential createCredential (final Configuration configuration) {

        final Map<String, String> passwordMap = new HashMap<String, String>();
        final KeyStoreCredentialResolver resolver;
        final KeyStore keystore;
        final Criterion criterion;
        final String password;
        final String keyStorePath;
        final String keyEntryId;
        final String keyStoreEntryPassword;
        final CriteriaSet criteriaSet;
        final CredentialProvider customCredentialProvider =
                configuration.getServiceProviderCustomCredentialProvider();
        Credential credential = null;

        try {

            if (null != customCredentialProvider) {

                credential = customCredentialProvider.createCredential();
            } else {

                keyStorePath = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH, "");
                password = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD, "");
                keyEntryId = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_ENTRY_ID, "");
                keyStoreEntryPassword = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_STORE_ENTRY_PASSWORD, "");

                Logger.info(SamlUtils.class, "Creating the credentials, using: " + password +
                        ", key store path: " + keyStorePath);

                final String keyStoreType = configuration.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_KEY_STORE_TYPE, KeyStore.getDefaultType());

                keystore = readKeyStoreFromFile
                        (keyStorePath, password, keyStoreType);

                passwordMap.put(keyEntryId, keyStoreEntryPassword);
                resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

                criterion = new EntityIdCriterion(keyEntryId);
                criteriaSet = new CriteriaSet();
                criteriaSet.add(criterion);
                credential = resolver.resolveSingle(criteriaSet);
            }
        } catch (ResolverException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException("Something went wrong reading credentials", e);
        }

        return credential;
    } // createCredential.

    /**
     * Get the SP credential
     * @return Credential
     */
    public static Credential getCredential(final Configuration configuration) {

        if (!credentialMap.containsKey(configuration.getSiteName())) {

            credentialMap.put(configuration.getSiteName(),
                    createCredential(configuration));
        }

        return credentialMap.get(configuration.getSiteName());
    } // getCredential.

    private static Credential createIdpCredential (final Configuration configuration) {

        KeyPair keyPair = null;
        final CredentialProvider customCredentialProvider =
                configuration.getIdProviderCustomCredentialProvider();
        Credential idpCredential = null;

        try {

            if (null != customCredentialProvider) {

                idpCredential = customCredentialProvider.createCredential();
            } else {
                // this fallback generates just a random keypair not very useful to validate the signature.
                keyPair = KeySupport.generateKeyPair("RSA", 1024, null);
                idpCredential =
                        CredentialSupport.getSimpleCredential(keyPair.getPublic(), keyPair.getPrivate());
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        return idpCredential;
    } // createIdpCredential.

    public static Credential getIdPCredentials (final Configuration configuration) {

        if (!idpCredentialMap.containsKey(configuration)) {


            idpCredentialMap.put(configuration.getSiteName(),
                    createIdpCredential(configuration));
        }

        return idpCredentialMap.get(configuration.getSiteName());
    } // getIdPCredentials.

    /**
     * Convert to String an {@link XMLObject}
     * @param object {@link XMLObject}
     * @return String
     */
    public static String toXMLObjectString(final XMLObject object) {

        final Element element =
                (object instanceof SignableSAMLObject &&
                SignableSAMLObject.class.cast(object).isSigned() &&
                object.getDOM() != null)?
                        object.getDOM(): toElement(object);

        return toElementString(element);
    } // toString

    /**
     * Convert to String an {@link Element}
     * @param element {@link Element}
     * @return String
     */
    public static String toElementString (final Element element) {

        final Transformer transformer;
        final StreamResult result;
        final DOMSource source;
        String xmlString = null;

        try {

            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            result = new StreamResult(new StringWriter());
            source = new DOMSource(element);

            transformer.transform(source, result);
            xmlString = result.getWriter().toString();
        } catch (TransformerException  e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
        }

        return xmlString;
    } // toString.


    public static Element toElement (final XMLObject object)  {

        final Marshaller out =
                marshallerFactory.getMarshaller(object);

        try {

            out.marshall(object);
        } catch (MarshallingException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
        }

        return object.getDOM();
    } // toElement.


    /**
     * Invoke a message handler chain
     * @param handlerChain {@link BasicMessageHandlerChain}
     * @param context MessageContext
     */
    public static <T> void invokeMessageHandlerChain (final BasicMessageHandlerChain<T> handlerChain,
                                                  final MessageContext<T> context) {

        try {

            handlerChain.initialize();
            handlerChain.doInvoke(context);
        } catch (ComponentInitializationException | MessageHandlerException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // invokeMessageHandlerChain.

    /**
     * Check if we can read the KeyStore from file using the SAML properties.
     *
     * @param samlProperties {@link Properties} with the values needed.
     * @return
     */
    public static Set<String> validateKeyStore(Properties samlProperties) {

        Set<String> otherErrors = new HashSet<>();

        final String pathToKeyStore = samlProperties.getProperty(DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH);
        final String keyStorePassword = samlProperties.getProperty(DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD);
        final String keyStoreType = samlProperties.getProperty(DotSamlConstants.DOTCMS_SAML_KEY_STORE_TYPE, KeyStore.getDefaultType());
        final String keyEntryId = samlProperties.getProperty(DotSamlConstants.DOTCMS_SAML_KEY_ENTRY_ID);
        final String keyStoreEntryPassword = samlProperties.getProperty(DotSamlConstants.DOTCMS_SAML_KEY_STORE_ENTRY_PASSWORD);

        if ( pathToKeyStore != null
            && keyStorePassword != null
            && keyStoreType != null
            && keyEntryId != null
            && keyStoreEntryPassword != null) {

            try {
                final KeyStore keystore = readKeyStoreFromFile(pathToKeyStore, keyStorePassword, keyStoreType);

                final Map<String, String> passwordMap = new HashMap<>();
                passwordMap.put(keyEntryId, keyStoreEntryPassword);

                final KeyStoreCredentialResolver resolver =
                    new KeyStoreCredentialResolver(keystore, passwordMap);

                final Criterion criterion = new EntityIdCriterion(keyEntryId);
                final CriteriaSet criteriaSet = new CriteriaSet();
                criteriaSet.add(criterion);
                resolver.resolveSingle(criteriaSet);

            } catch (DotSamlException e){
                otherErrors.add("Error reading Key Store");
            } catch (ResolverException e) {
                otherErrors.add("Error reading credentials");
            }
        }
        return otherErrors;
    } // validateKeyStore.

    /**
     * Check if the File Path exists, can access and read.
     *
     * @param properties {@link Properties} (Key, Value) = (PropertyName, FilePath).
     * @param filePathPropertyKeys keys name that have File path in the value.
     * @return
     */
    public static Set<String> validateFiles(Properties properties, Set<String> filePathPropertyKeys) {
        Set<String> missingFiles = new HashSet<>();

        for (String fileField : filePathPropertyKeys) {
            //If field is missing we don't need to validate.
            if ( properties.getProperty(fileField) == null ) {
                continue;
            }

            //Check if the file exists.
            String filePath = properties.getProperty(fileField);
            File file = new File(filePath);
            if ( !file.exists() && !file.canRead() ) {
                try {
                    //Let's try with the URI.
                    URI uri = new URI(filePath);
                    file = new File(uri);
                    if ( !file.exists() && !file.canRead() ) {
                        missingFiles.add(filePath);
                    }
                } catch (URISyntaxException e){
                    Logger.debug(SamlUtils.class, "Problem reading file from URI: " + filePath, e);
                    missingFiles.add(filePath);
                }
            }
        }
        return missingFiles;
    } // validateFiles.

    /**
     * Validates that properties contains the keys required.
     *
     * @param properties {@link Properties} (Key, Value) = (PropertyName, FilePath).
     * @param keysRequired Property keys required to be in the samlProperties.
     * @return
     */
    public static Set<String> getMissingProperties(Properties properties, Set<String> keysRequired) {
        Set<String> missingProperties = new HashSet<>();
        for (String s : keysRequired) {
            if ( properties.getProperty(s) == null || properties.getProperty(s).equals("") ) {
                missingProperties.add(s);
            }
        }
        return missingProperties;
    } // getMissingProperties.

} // E:O:F:SamlUtils.
