package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.init.DefaultInitializer;
import com.dotcms.plugin.saml.v3.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;

/**
 * Encapsulates constants for the dot SAML SP
 * @author jsanca
 */
public final class DotSamlConstants {

    /**
     * Key for dotmarketing-config.properties to get the Saml protocol binding, by default use {@link org.opensaml.saml.common.xml.SAMLConstants}.SAML2_ARTIFACT_BINDING_URI
     */
    public static final String DOTCMS_SAML_PROTOCOL_BINDING = "dotcms.saml.protocol.binding";

    /**
     * Key for dotmarketing-config.properties
     * If you have already set a idp-metadata, this value will be taken from it, otherwise you have to set it on the dotCMS properties.
     * If the value is not present will got an exception on runtime.
     * This value is the Redirect SSO url (usually Shibboleth), which is the one to be redirect when the user is not logged on dotCMS.
     */
    public static final String DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL = "dotcms.saml.identity.provider.destinationsso.url";

    /**
     * Key for dotmarketing-config.properties
     * This is a mandatory property, if you do not set it will got an exception
     * This url is the soap endpoint to get the user assertion from the SAML Artifact id.
     */
    public static final String DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL  = "dotcms.saml.artifact.resolution.service.url";

    /**
     * Key for dotmarketing-config.properties
     * This is the customer endpoint url, which means the url where to be redirected when the user gets log back to dotcms.
     * You can set it for instance to http://[domain]/c in order to get back to the landing page, dotCMS will redirect as soon as do the
     * artificial login with the SAML information, to the original request.
     */
    public static final String DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL  = "dotcms.saml.assertion.customer.endpoint.url";

    /**
     * Key for dotmarketing-config.properties
     * For the provider issue which is the identifier for the sender, by default is: DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE
     * Usually you overrides it to your host domain, for instance our id could be http://dotcms.com.
     */
    public static final String DOTCMS_SAML_SERVICE_PROVIDER_ISSUER = "dotcms.saml.service.provider.issuer";

    /**
     * Default value for the DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER
     */
    public static final String DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE = "com.dotcms.plugin.saml.v3.issuer";

    /**
     * If you want to allow to create an user that does not exists on the IdP, set this to true, otherwise false.
     * By default it is false, so won't allow to create the user, however the Idp will be the final responsable to decided if the user could be or not created.
     */
    public static final String DOTCMS_SAML_POLICY_ALLOW_CREATE = "dotcms.saml.policy.allowcreate";

    /**
     * SAML allows several formats, such as Kerberos, email, Windows Domain Qualified Name, etc.
     * By default dotcms use: {@link org.opensaml.saml.saml2.core.NameIDType}.TRANSIENT.
     * See More on {@link org.opensaml.saml.saml2.core.NameIDType}
     */
    public static final String DOTCMS_SAML_POLICY_FORMAT = "dotcms.saml.policy.format";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: {@link org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration}.MINIMUM, but you can set any different you setting the value (non-case sensitive)
     * For instance:
     *
     * <code>
     * dotcms.saml.authn.comparisontype=BETTER
     * </code>
     *
     * Will use {@link org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration}.BETTER instead of MINIMUN.
     */
    public static final String DOTCMS_SAML_AUTHN_COMPARISON_TYPE = "dotcms.saml.authn.comparisontype";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: {@link org.opensaml.saml.saml2.core.AuthnContext}.PASSWORD_AUTHN_CTX, but you can override it just adding the context class ref you want.
     */
    public static final String DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF = "dotcms.saml.authn.context.class.ref";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: /SPKeystore.jks, but if you want to use a different key store path you can override it on the properties file.
     * By default the KeyStore is expected to be on the classpath, however you can use the prefix: file:// in order to assign a file system path for it.
     */
    public static final String DOTCMS_SAML_KEY_STORE_PATH = "dotcms.saml.keystore.path";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "password", but if you want to use a different key store password you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_STORE_PASSWORD = "dotcms.saml.keystore.password";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "SPKey", but if you want to use a different key store password you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_ENTRY_ID = "dotcms.saml.keyentryid";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "password", but if you want to use a different key store password you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_STORE_ENTRY_PASSWORD = "dotcms.saml.keystore.entry.password";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: <code>{@link java.security.KeyStore}.getDefaultType()</code>, but if you want to use a different key type password you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_STORE_TYPE = "dotcms.saml.keystore.type";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: {@link org.opensaml.xmlsec.signature.support.SignatureConstants}.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, but you can override it just adding the algorithm you want.
     */
    public static final String DOTCMS_SAML_SIGNATURE_CANONICALIZATION_ALGORITHM = "dotcms.saml.signature.canonicalization.algorithm";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: 1000, but you can override it just adding the new time you want.
     */
    public static final String DOT_SAML_CLOCK_SKEW = "dotcms.saml.clock.skew";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: 2000, but you can override it just adding the new time you want.
     */
    public static final String DOT_SAML_MESSAGE_LIFE_TIME = "dotcms.saml.message.life.time";


    /**
     * Optional Key for dotmarketing-config.properties
     * By default dotcms do not use any filter, but you can override it just adding the filter you want.
     * For instance, sometimes LDAP providers use a prefix for an external roles or so, you can remove this prefix by setting this prop.
     */
    public static final String DOT_SAML_REMOVE_ROLES_PREFIX = "dotcms.saml.remove.roles.prefix";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "mail", but you can override it just adding the mail attribute name you want.
     * "mail" will be the expected field name from the SOAP Artifact comming from the OpenSaml resolution artifact.
     */
    public static final String DOT_SAML_EMAIL_ATTRIBUTE = "dotcms.saml.email.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "givenName", but you can override it just adding the first name attribute name you want.
     * "givenName" will be the expected field name from the SOAP Artifact comming from the OpenSaml resolution artifact.
     */
    public static final String DOT_SAML_FIRSTNAME_ATTRIBUTE = "dotcms.saml.firstname.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "sn", but you can override it just adding the last name attribute name you want.
     * "sn" will be the expected field name from the SOAP Artifact comming from the OpenSaml resolution artifact.
     */
    public static final String DOT_SAML_LASTNAME_ATTRIBUTE = "dotcms.saml.lastname.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "authorisations", but you can override it just adding the roles attribute name you want.
     * "authorisations" will be the expected field name from the SOAP Artifact comming from the OpenSaml resolution artifact.
     */
    public static final String DOT_SAML_ROLES_ATTRIBUTE = "dotcms.saml.roles.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: {@link DefaultInitializer} but you can override by adding a full class name to this property.
     */
    public static final String DOT_SAML_INITIALIZER_CLASS_NAME = "dotcms.saml.initializer.classname";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: {@link com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration} but you can override by addding a full class name to this property.
     */
    public static final String DOT_SAML_CONFIGURATION_CLASS_NAME = "dotcms.saml.configuration.classname";

    /**
     * Key for dotmarketing-config.properties
     * In case you have a idp-metadata.xml you can get it from the classpath or file system.
     * For the classpath you overrides the property with the right path in your class path.
     * If you want to get the XML from the file system use the prefix; file://
     */
    public static final String DOTCMS_SAML_IDP_METADATA_PATH = "dotcms.saml.idp.metadata.path";

    /**
     * Key for dotmarketing-config.properties
     * By default dot cms use {@link DotSamlConstants}.DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE, in case you need to use a differente
     * feel free to override this property.
     */
    public static final String DOT_SAML_IDP_METADATA_PROTOCOL = "dotcms.saml.idp.metadata.protocol";

    /**
     * Default value for the metadata protocol see {@link DotSamlConstants}.DOT_SAML_IDP_METADATA_PROTOCOL
     */
    public static final String DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE = "urn:oasis:names:tc:SAML:2.0:protocol";

    /**
     * Key for dotmarketing-config.properties
     * By default {@link DefaultMetaDescriptorServiceImpl} is what we use to parser the idp metadata XML file
     * however if you have you own implementation of {@link MetaDescriptorService} you can override it.
     */
    public static final String DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME = "dotcms.saml.idp.metadata.parser.classname";

    /**
     * Key for dotmarketing-config.properties
     * By default we do not filter anything, but if there is some special cases (url's) you want to avoid the authentication check,
     * add here the values comma separated.
     */
    public static final String DOT_SAML_ACCESS_FILTER_VALUES = "dotcms.saml.access.filter.values";

    /**
     * Key for dotmarketing-config.properties
     * By default the system will do the verification of the assertion signature, if for some reason you want to avoid it
     * feel free to set it to "false".
     */
    public static final String DOT_SAML_VERIFY_ASSERTION_SIGNATURE = "dotcms.saml.verify.assertion.signature";

    /**
     * Key for dotmarketing-config.properties
     * By default the system will do the verification of the profile signature, if for some reason you want to avoid it
     * feel free to set it to "false".
     */
    public static final String DOT_SAML_VERIFY_SIGNATURE_PROFILE = "dotcms.saml.verify.signature.profile";

    /**
     * Key for dotmarketing-config.properties
     * By default the system will do the verification of the signature credentials, if for some reason you want to avoid it
     * feel free to set it to "false".
     */
    public static final String DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS = "dotcms.saml.verify.signature.credentials";

    /**
     * Key for dotmarketing-config.properties
     * In case you need a custom credentials for the Service Provider (DotCMS) overrides the
     * implementation class on the configuration properties.
     */
    public static final String DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME = "dotcms.saml.service.provider.custom.credential.provider.classname";

    /**
     * Key for dotmarketing-config.properties
     * In case you need a custom credentials for the ID Provider (DotCMS) overrides the
     * implementation class on the configuration properties.
     */
    public static final String DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME = "dotcms.saml.id.provider.custom.credential.provider.classname";

    /**
     * Key for dotmarketing-config.properties
     * By default true, overrides if you want the assertions signed.
     */
    public static final String DOTCMS_SAML_WANT_ASSERTIONS_SIGNED = "dotcms.saml.want.assertions.signed";

    /**
     * Key for dotmarketing-config.properties
     * By default true, overrides if you want the authorization requests signed.
     */
    public static final String DOTCMS_SAML_AUTHN_REQUESTS_SIGNED = "dotcms.saml.authn.requests.signed";

    /**
     * Key for dotmarketing-config.properties
     * By default we use: "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent", you can overrides if need others by comma separated.
     */
    public static final String DOTCMS_SAML_NAME_ID_FORMATS = "dotcms.saml.name.id.formats";

    /**
     * Key for dotmarketing-config.properties
     * By default we use: "{@link DotSamlConstants}.DOT_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE", you can override the path to mapping the metadata with whatever value you need.
     */
    public static final String DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH = "dotcms.saml.sevice.provider.custom.metadata.path";

    /**
     * Default value "/dotsaml3sp/metadata.xml" for the {@link DotSamlConstants}.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH
     * This is the path you have to use in the metadata providers on the open saml idp (for instance shibboleth) or you can override by using {@link DotSamlConstants}.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH
     */
    public static final String DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE = "/dotsaml3sp/metadata.xml";
} // E:O:F:DotSamlConstants.