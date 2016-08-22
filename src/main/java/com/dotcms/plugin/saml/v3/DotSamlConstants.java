package com.dotcms.plugin.saml.v3;

import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotmarketing.util.Config;

/**
 * Encapsulates constant for the dot SAML SP
 * @author jsanca
 */
public final class DotSamlConstants {

    /**
     * Key for dotmarketing-config.properties to get the Saml protocol binding, by default use SAMLConstants.SAML2_ARTIFACT_BINDING_URI
     */
    public static final String DOTCMS_SAML_PROTOCOL_BINDING = "dotcms.saml.protocol.binding";

    /**
     * Key for dotmarketing-config.properties
     * This is a mandatory property, if you do not set it will got an exception
     */
    public static final String DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL = "dotms.saml.identity.provider.destinationsso.url";

    /**
     * Key for dotmarketing-config.properties
     * This is a mandatory property, if you do not set it will got an exception
     */
    public static final String DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL  = "dotms.saml.artifact.resolution.service.url";

    /**
     * Key for dotmarketing-config.properties
     * For the provider issue which is the identifier for the sender, by default is: DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE
     */
    public static final String DOTCMS_SAML_SERVICE_PROVIDER_ISSUER = "dotms.saml.service.provider.issuer";

    /**
     * Default value for the DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER
     */
    public static final String DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE = "com.dotcms.plugin.saml.v3.issuer";

    /**
     * If you want to allow to create an user that does not exists on the IdP, set this to true, otherwise false.
     * By default it is true, so allows the used to be created.
     */
    public static final String DOTCMS_SAML_POLICY_ALLOW_CREATE = "dotcms.saml.policy.allowcreate";

    /**
     * SAML allows several formats, such as Kerberos, email, Windows Domain Qualified Name, etc.
     * By default dotcms use: NameIDType.TRANSIENT.
     * See More on {@link org.opensaml.saml.saml2.core.NameIDType}
     */
    public static final String DOTCMS_SAML_POLICY_FORMAT = "dotcms.saml.policy.format";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: AuthnContextComparisonTypeEnumeration.MINIMUM, but you can set any different you setting the value (non-case sensitive)
     * For instance:
     *
     * <code>
     * dotcms.saml.authn.comparisontype=BETTER
     * </code>
     *
     * Will use AuthnContextComparisonTypeEnumeration.BETTER instead of MINIMUN.
     */
    public static final String DOTCMS_SAML_AUTHN_COMPARISON_TYPE = "dotcms.saml.authn.comparisontype";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: AuthnContext.PASSWORD_AUTHN_CTX, but you can override it just adding the context class ref you want.
     */
    public static final String DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF = "dotcms.saml.authn.context.class.ref";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: /SPKeystore.jks, but if you want to use a different key store path you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_STORE_PATH = "dotcms.saml.keystore.path";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "password", but if you want to use a different key store password you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_STORE_PASSWORD = "dotcms.saml.keystore.password";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: <code>KeyStore.getDefaultType()</code>, but if you want to use a different key type password you can override it on the properties file.
     */
    public static final String DOTCMS_SAML_KEY_STORE_TYPE = "dotcms.saml.keystore.type";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, but you can override it just adding the algorithm you want.
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
     */
    public static final String DOT_SAML_REMOVE_ROLES_PREFIX = "dotcms.saml.remove.roles.prefix";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "mail", but you can override it just adding the mail attribute name you want.
     */
    public static final String DOT_SAML_EMAIL_ATTRIBUTE = "dotcms.saml.email.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "givenName", but you can override it just adding the first name attribute name you want.
     */
    public static final String DOT_SAML_FIRSTNAME_ATTRIBUTE = "dotcms.saml.firstname.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "sn", but you can override it just adding the last name attribute name you want.
     */
    public static final String DOT_SAML_LASTNAME_ATTRIBUTE = "dotcms.saml.lastname.attribute";

    /**
     * Key for dotmarketing-config.properties
     * By default dotcms use: "authorisations", but you can override it just adding the roles attribute name you want.
     */
    public static final String DOT_SAML_ROLES_ATTRIBUTE = "dotcms.saml.roles.attribute";
} // E:O:F:DotSamlConstants.
