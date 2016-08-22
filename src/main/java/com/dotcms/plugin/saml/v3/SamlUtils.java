package com.dotcms.plugin.saml.v3;

import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
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
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utils method for the Saml
 * @author jsanca
 */
public class SamlUtils {

    private final static RandomIdentifierGenerationStrategy secureRandomIdGenerator =
            new RandomIdentifierGenerationStrategy();

    private final static XMLObjectBuilderFactory builderFactory =
            XMLObjectProviderRegistrySupport.getBuilderFactory();

    private static final String DEFAULT_ELEMENT_NAME = "DEFAULT_ELEMENT_NAME";

    private static Credential credential;

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

        final String ipDSSODestination = getIPDSSODestination();
        final AuthnRequest authnRequest = buildSAMLObject(AuthnRequest.class);

        // this ensure that the message redirected is not too old
        authnRequest.setIssueInstant(new DateTime());

        // IDP url
        if (!UtilMethods.isSet(ipDSSODestination)) {

            throw new DotSamlException ("The property: " + DotSamlConstants.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL +
                " must be set on the dotmarketing-config.properties");
        }

        authnRequest.setDestination(ipDSSODestination);

        // Get the protocol from the user, or use a default one: SAMLConstants.SAML2_ARTIFACT_BINDING_URI
        authnRequest.setProtocolBinding
                (Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_PROTOCOL_BINDING,
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

        return Config.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL, null);
    } // getIPDSSODestination.

    public static  String getAssertionConsumerEndpoint(final HttpServletRequest request) {
        // this is the same original request. Consequently where should be redirected when the authentication is done.
        return new StringBuilder(request.getRequestURI()).append('?')
                .append(request.getQueryString()).toString();
    } // getAssertionConsumerEndpoint/

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

        return Config.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER,
                    DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE);
    } // getSPIssuerValue.

    /**
     * Return the policy for the Name ID (which is the IdP identifier for the user)
     * @return NameIDPolicy
     */
    public static NameIDPolicy buildNameIdPolicy() {

        final NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);

        // True if you want that when the  user does not exists, allows to create
        nameIDPolicy.setAllowCreate(Config.getBooleanProperty(DotSamlConstants.DOTCMS_SAML_POLICY_ALLOW_CREATE, true));

        // it supports several formats, such as Kerberos, email, Windows Domain Qualified Name, etc.
        nameIDPolicy.setFormat(Config.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_POLICY_FORMAT,
                // “The transient identifier is a random identifier that does not have any connection to the user. A transient identifier will be different for every time the user signs in.”
                NameIDType.TRANSIENT));

        return nameIDPolicy;
    } // buildNameIdPolicy.

    // todo: keep in mind more than one authentication can be defined

    /**
     * Build the Authentication context, with the login and password strategies
     * @return RequestedAuthnContext
     */
    public static RequestedAuthnContext buildRequestedAuthnContext() {

        final RequestedAuthnContext requestedAuthnContext =
                buildSAMLObject(RequestedAuthnContext.class);

        requestedAuthnContext.setComparison
                (getAuthnContextComparisonTypeEnumeration());

        final AuthnContextClassRef passwordAuthnContextClassRef =
                buildSAMLObject(AuthnContextClassRef.class);

        passwordAuthnContextClassRef.setAuthnContextClassRef
                (Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF,
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

        AuthnContextComparisonTypeEnumeration comparisonTypeEnumeration =
                AuthnContextComparisonTypeEnumeration.MINIMUM;

        final String enumName = Config.getStringProperty
                (DotSamlConstants.DOTCMS_SAML_AUTHN_COMPARISON_TYPE, null);

        if (UtilMethods.isSet(enumName)) {

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

        endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        endpoint.setLocation(getIPDSSODestination());

        return endpoint;
    } // getIdentityProviderDestinationEndpoint.

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
        InputStream inputStream = null;

        try {

            keyStoreType = Config.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_KEY_STORE_TYPE, KeyStore.getDefaultType());
            keystore = KeyStore.getInstance(keyStoreType);
            inputStream = SamlUtils.class.getResourceAsStream(pathToKeyStore);
            keystore.load(inputStream, keyStorePassword.toCharArray());
        } catch (Exception e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException("Something went wrong reading keystore", e);
        } finally {

            IOUtils.closeQuietly(inputStream);
        }

        return keystore;
    } // readKeystoreFromFile.

    private static void createCredential () {

        final Map<String, String> passwordMap = new HashMap<String, String>();
        final KeyStoreCredentialResolver resolver;
        final KeyStore keystore;
        final Criterion criterion;
        final String password;
        final String keyStorePath;
        final CriteriaSet criteriaSet;

        try {

            keyStorePath = Config.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH, KEY_STORE_PATH);
            password = Config.getStringProperty(
                    DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD, KEY_STORE_PASSWORD);

            Logger.info(SamlUtils.class, "Creating the credentials, using: " + password +
                    ", key store path: " + keyStorePath);

            keystore = readKeyStoreFromFile
                         (keyStorePath, password);

            passwordMap.put(KEY_ENTRY_ID, KEY_STORE_ENTRY_PASSWORD);
            resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

            criterion = new EntityIdCriterion(KEY_ENTRY_ID);
            criteriaSet = new CriteriaSet();
            criteriaSet.add(criterion);
            credential = resolver.resolveSingle(criteriaSet);
        } catch (ResolverException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException("Something went wrong reading credentials", e);
        }
    } // createCredential.

    /**
     * Get the credential
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

    /**
     * Convert to String an {@link XMLObject}
     * @param object {@link XMLObject}
     * @return String
     */
    public static String toString(final XMLObject object) {

        final Element element =
                (object instanceof SignableSAMLObject &&
                SignableSAMLObject.class.cast(object).isSigned() &&
                object.getDOM() != null)?
                        object.getDOM(): toElement(object);

        return toString(element);
    } // toString

    /**
     * Convert to String an {@link Element}
     * @param element {@link Element}
     * @return String
     */
    public static String toString (final Element element) {

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
        } catch (TransformerConfigurationException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
        } catch (TransformerException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
        }

        return xmlString;
    } // toString.


    public static Element toElement (final XMLObject object)  {

        final Marshaller out = XMLObjectProviderRegistrySupport.
                getMarshallerFactory().getMarshaller(object);
        try {

            out.marshall(object);
        } catch (MarshallingException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
        }

        return object.getDOM();
    } // toElement.
} // E:O:F:SamlUtils.
