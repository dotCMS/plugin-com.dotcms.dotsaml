package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;
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
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    private static volatile Credential credential;

    private static volatile Credential idpCredential;

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
    public static AuthnRequest buildAuthnRequest(final HttpServletRequest request) {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final String ipDSSODestination = getIPDSSODestination();
        final AuthnRequest authnRequest = buildSAMLObject(AuthnRequest.class);

        // this ensure that the message redirected is not too old
        authnRequest.setIssueInstant(new DateTime());

        // IDP url
        if (!isSet(ipDSSODestination)) {

            throw new DotSamlException ("The property: " + DotSamlConstants.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL +
                " must be set on the dotmarketing-config.properties");
        }

        authnRequest.setDestination(ipDSSODestination);

        // Get the protocol from the user, or use a default one: SAMLConstants.SAML2_ARTIFACT_BINDING_URI
        authnRequest.setProtocolBinding
                (configuration.getStringProperty(DotSamlConstants.DOTCMS_SAML_PROTOCOL_BINDING,
                        SAMLConstants.SAML2_ARTIFACT_BINDING_URI));

        // this is the address that receives the SAML Assertion, after a successful authentication on the IdP.
        authnRequest.setAssertionConsumerServiceURL(getAssertionConsumerEndpoint(request));

        // this is a uid or random id just to identified the response.
        authnRequest.setID(generateSecureRandomId());

        // id for the sender
        authnRequest.setIssuer(buildIssuer());

        authnRequest.setNameIDPolicy(buildNameIdPolicy());
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext());

        return authnRequest;
    } // buildAuthnRequest.

    /**
     * Gets from the dotmarketing-config.properties the destination sso url
     * @return String
     */
    public static String getIPDSSODestination() {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());

        final String redirectIdentityProviderDestinationSSOURL =
                    configuration.getRedirectIdentityProviderDestinationSSOURL();

        // first check the meta data info., secondly the configuration
        return (null != redirectIdentityProviderDestinationSSOURL)?
                redirectIdentityProviderDestinationSSOURL:
                configuration.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL, null);
    } // getIPDSSODestination.

    public static  String getAssertionConsumerEndpoint(final HttpServletRequest request) {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());

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
    public static Issuer buildIssuer() {

        final Issuer issuer =
                buildSAMLObject(Issuer.class);
        issuer.setValue(getSPIssuerValue());

        return issuer;
    } // buildIssuer.

    /**
     * Get the od for the Issuer, it is the SP identifier on the IdP
     * @return String
     */
    public static String getSPIssuerValue() {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());

        return configuration.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER,
                    DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE);
    } // getSPIssuerValue.

    /**
     * Return the policy for the Name ID (which is the IdP identifier for the user)
     * @return NameIDPolicy
     */
    public static NameIDPolicy buildNameIdPolicy() {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);

        // True if you want that when the  user does not exists, allows to create
        nameIDPolicy.setAllowCreate(configuration.getBooleanProperty(DotSamlConstants.DOTCMS_SAML_POLICY_ALLOW_CREATE, true));

        // it supports several formats, such as Kerberos, email, Windows Domain Qualified Name, etc.
        nameIDPolicy.setFormat(configuration.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_POLICY_FORMAT,
                // “The transient identifier is a random identifier that does not have any connection to the user. A transient identifier will be different for every time the user signs in.”
                NameIDType.TRANSIENT));

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
    public static RequestedAuthnContext buildRequestedAuthnContext() {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final RequestedAuthnContext requestedAuthnContext =
                buildSAMLObject(RequestedAuthnContext.class);

        requestedAuthnContext.setComparison
                (getAuthnContextComparisonTypeEnumeration());

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
    public static AuthnContextComparisonTypeEnumeration getAuthnContextComparisonTypeEnumeration() {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
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
    public static Endpoint getIdentityProviderDestinationEndpoint() {

        final SingleSignOnService endpoint = buildSAMLObject(SingleSignOnService.class);

        // todo: based on the configuration use redirect or post
        endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        endpoint.setLocation(getIPDSSODestination());

        return endpoint;
    } // getIdentityProviderDestinationEndpoint.

    /**
     * Builds the artifact resolver using the resolution service url from the
     * dotmarketing-config.properties, if it is not set will throw an exception.
     *
     * @param artifact {@link Artifact}
     * @return ArtifactResolve
     */
    public static ArtifactResolve buildArtifactResolve(final Artifact artifact) {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final ArtifactResolve artifactResolve = buildSAMLObject(ArtifactResolve.class);
        final String artifactResolutionService = configuration.getStringProperty(
                DotSamlConstants.DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL, null);

        if (!isSet(artifactResolutionService)) {

            throw new DotSamlException ("The property: " + DotSamlConstants.DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL +
                    " must be set on the dotmarketing-config.properties");
        }

        artifactResolve.setIssuer(buildIssuer());
        artifactResolve.setIssueInstant(new DateTime());
        artifactResolve.setID(generateSecureRandomId());
        artifactResolve.setDestination(artifactResolutionService);
        artifactResolve.setArtifact(artifact);

        return artifactResolve;
    } // buildArtifactResolve

    /**
     * Get the Assertion decrypted
     * @param artifactResponse {@link ArtifactResponse}
     * @return Assertion
     */
    public static Assertion getAssertion(final ArtifactResponse artifactResponse) {

        final EncryptedAssertion encryptedAssertion = getEncryptedAssertion(artifactResponse);
        final Assertion assertion = decryptAssertion(encryptedAssertion); /// this is the user message itself

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
    public static Assertion decryptAssertion(final EncryptedAssertion encryptedAssertion) {

        Assertion assertion = null;
        final StaticKeyInfoCredentialResolver keyInfoCredentialResolver =
                new StaticKeyInfoCredentialResolver(getCredential());

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

                Logger.error(SamlUtils.class, "Validation failed with credential", ignore);
            }
        }

        throw new SignatureException("Signature cannot be validated");
    } // validateSignature.

    /**
     * Does the verification of the assertiong
     * @param assertion {@link Assertion}
     */
    public static void verifyAssertionSignature(final Assertion assertion) {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final SAMLSignatureProfileValidator profileValidator;

        if (!assertion.isSigned()) {

            throw new DotSamlException("The SAML Assertion was not signed");
        }

        try {

            if (configuration.isVerifySignatureProfileNeeded()) {

                profileValidator = new SAMLSignatureProfileValidator();
                profileValidator.validate(assertion.getSignature());
            } else {

                Logger.info(SamlUtils.class, "Skipping the Verify Signature Profile check");
            }

            // Ask on the config if the app wants signature validator
            if (configuration.isVerifySignatureCredentialsNeeded()) {

                if (null != configuration.getSigningCredentials ()) {

                    validateSignature(assertion, configuration.getSigningCredentials ());
                } else {

                    SignatureValidator.validate(assertion.getSignature(), getIdPCredentials());
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

    private static final String KEY_STORE_PASSWORD = "password";
    private static final String KEY_STORE_ENTRY_PASSWORD = "password";
    private static final String KEY_STORE_PATH = "/SPKeystore.jks";
    private static final String KEY_ENTRY_ID = "SPKey";

    /**
     * Read from the key store using a given password
     * @param pathToKeyStore {@link String}
     * @param keyStorePassword {@link String}
     * @return KeyStore
     */
    public static KeyStore readKeyStoreFromFile(final String pathToKeyStore,
                                                final String keyStorePassword) {

        final KeyStore keystore;
        final String keyStoreType;
        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        InputStream inputStream = null;

        try {

            keyStoreType = configuration.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_KEY_STORE_TYPE, KeyStore.getDefaultType());
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

    public static void createCredential () {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
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

        try {

            if (null != customCredentialProvider) {

                credential = customCredentialProvider.createCredential();
            } else {

                keyStorePath = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH, KEY_STORE_PATH);
                password = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD, KEY_STORE_PASSWORD);
                keyEntryId = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_ENTRY_ID, KEY_ENTRY_ID);
                keyStoreEntryPassword = configuration.getStringProperty(
                        DotSamlConstants.DOTCMS_SAML_KEY_STORE_ENTRY_PASSWORD, KEY_STORE_ENTRY_PASSWORD);

                Logger.info(SamlUtils.class, "Creating the credentials, using: " + password +
                        ", key store path: " + keyStorePath);

                keystore = readKeyStoreFromFile
                        (keyStorePath, password);

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
    } // createCredential.

    /**
     * Get the SP credential
     * @return Credential
     */
    public static Credential getCredential() {

        if (null == credential) {

            synchronized (SamlUtils.class) {

                if (null == credential) {

                    createCredential();
                }
            }
        }

        return credential;
    } // getCredential.

    private static void createIdpCredential () {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        KeyPair keyPair = null;
        final CredentialProvider customCredentialProvider =
                configuration.getIdProviderCustomCredentialProvider();

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
    } // createIdpCredential.

    public static Credential getIdPCredentials () {

        if (null == idpCredential) {

            synchronized (SamlUtils.class) {

                if (null == idpCredential) {

                    createIdpCredential();
                }
            }
        }

        return idpCredential;
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
} // E:O:F:SamlUtils.
