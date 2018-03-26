package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.init.DefaultInitializer;
import com.dotcms.plugin.saml.v3.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;

/**
 * Encapsulates constants for the dot SAML SP
 *
 * @author jsanca
 */
public final class DotSamlConstants
{
	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * Used to get the Saml protocol binding, by default use
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.SAML2_ARTIFACT_BINDING_URI
	 */
	public static final String DOTCMS_SAML_PROTOCOL_BINDING = "protocol.binding";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * If you have already set a idp-metadata, this value will be taken from it,
	 * otherwise you have to set it on the dotCMS properties. If the value is
	 * not present will got an exception on runtime. This value is the Redirect
	 * SSO url (usually Shibboleth), which is the one to be redirect when the
	 * user is not logged on dotCMS.
	 */
	public static final String DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL = "identity.provider.destinationsso.url";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * If you have already set a idp-metadata, this value will be taken from it,
	 * otherwise you have to set it on the dotCMS properties. If the value is
	 * not present will got an exception on runtime. This value is the Redirect
	 * SLO (Logout) url (usually Shibboleth), which is the one to be redirect
	 * when the user does logout on dotCMS.
	 */
	public static final String DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SLO_URL = "identity.provider.destinationslo.url";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * This is the customer endpoint url, which means the url where to be
	 * redirected when the user gets log back to dotcms. You can set it for
	 * instance to http://[domain]/c in order to get back to the landing page,
	 * dotCMS will redirect as soon as do the artificial login with the SAML
	 * information, to the original request.
	 */
	public static final String DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL = "assertion.customer.endpoint.url";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * This is the logout service endpoint url, which means the url where to be
	 * redirected when the user gets log out. You can set it for instance to
	 * http://[domain]/c in order to get back to the page.
	 */
	public static final String DOT_SAML_LOGOUT_SERVICE_ENDPOINT_URL = "logout.service.endpoint.url";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * For the provider issue which is the identifier for the sender, by default
	 * is: DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE
	 * Usually you overrides it to your host domain, for instance our id could
	 * be http://dotcms.com.
	 */
	public static final String DOTCMS_SAML_SERVICE_PROVIDER_ISSUER = "service.provider.issuer";

	/**
	 * If you want to allow to create an user that does not exists on the IdP,
	 * set this to true, otherwise false. By default it is false, so won't allow
	 * to create the user, however the Idp will be the final responsable to
	 * decided if the user could be or not created.
	 */
	public static final String DOTCMS_SAML_POLICY_ALLOW_CREATE = "policy.allowcreate";

	/**
	 * SAML allows several formats, such as Kerberos, email, Windows Domain
	 * Qualified Name, etc. By default dotcms use:
	 * {@link org.opensaml.saml.saml2.core.NameIDType}.TRANSIENT. See More on
	 * {@link org.opensaml.saml.saml2.core.NameIDType}
	 */
	public static final String DOTCMS_SAML_NAME_ID_POLICY_FORMAT = "nameidpolicy.format";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotCMS uses:
	 * {@link org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration}.MINIMUM,
	 * but you can set any different you setting the value (non-case sensitive)
	 * For instance: <code>
	 * authn.comparisontype=BETTER
	 * </code> Will use
	 * {@link org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration}.BETTER
	 * instead of MINIMUN.
	 */
	public static final String DOTCMS_SAML_AUTHN_COMPARISON_TYPE = "authn.comparisontype";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotCMS uses:
	 * {@link org.opensaml.saml.saml2.core.AuthnContext}.PASSWORD_AUTHN_CTX, but
	 * you can override it just adding the context class ref you want.
	 */
	public static final String DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF = "authn.context.class.ref";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: /SPKeystore.jks, but if you want to use a
	 * different key store path you can override it on the properties file. By
	 * default the KeyStore is expected to be on the classpath, however you can
	 * use the prefix: file:// in order to assign a file system path for it.
	 */
	public static final String DOTCMS_SAML_KEY_STORE_PATH = "keystore.path";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "password", but if you want to use a different key
	 * store password you can override it on the properties file.
	 */
	public static final String DOTCMS_SAML_KEY_STORE_PASSWORD = "keystore.password";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "SPKey", but if you want to use a different key
	 * store password you can override it on the properties file.
	 */
	public static final String DOTCMS_SAML_KEY_ENTRY_ID = "keystore.entry.id";

	public static final String DOTCMS_SAML_KEY_ENTRY_ID_DEFAULT_VALUE = "dotsamlsp";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "password", but if you want to use a different key
	 * store password you can override it on the properties file.
	 */
	public static final String DOTCMS_SAML_KEY_STORE_ENTRY_PASSWORD = "keystore.entry.password";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use:
	 * <code>{@link java.security.KeyStore}.getDefaultType()</code>, but if you
	 * want to use a different key type password you can override it on the
	 * properties file.
	 */
	public static final String DOTCMS_SAML_KEY_STORE_TYPE = "keystore.type";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: 1000, but you can override it just adding the new
	 * time you want.
	 */
	public static final String DOT_SAML_CLOCK_SKEW = "clock.skew";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: 2000, but you can override it just adding the new
	 * time you want.
	 */
	public static final String DOT_SAML_MESSAGE_LIFE_TIME = "message.life.time";

	/**
	 * Optional Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms do not use any filter, but you can override it just
	 * adding the filter you want. For instance, sometimes LDAP providers use a
	 * prefix for an external roles or so, you can remove this prefix by setting
	 * this prop.
	 */
	public static final String DOT_SAML_REMOVE_ROLES_PREFIX = "remove.roles.prefix";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "mail", but you can override it just adding the
	 * mail attribute name you want. "mail" will be the expected field name from
	 * the Response coming from the OpenSaml post call.
	 */
	public static final String DOT_SAML_EMAIL_ATTRIBUTE = "attribute.email.name";

	/**
	 * Boolean value to allow to build a dummy email based on the NameID from
	 * the Idp when the email attribute from the IDP is not present. True will
	 * apply the email generation, false will throw 401 error.
	 */
	public static final String DOT_SAML_EMAIL_ATTRIBUTE_ALLOW_NULL = "attribute.email.allownull";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "givenName", but you can override it just adding
	 * the first name attribute name you want. "givenName" will be the expected
	 * field name from the Response comming from the OpenSaml post call.
	 */
	public static final String DOT_SAML_FIRSTNAME_ATTRIBUTE = "attribute.firstname.name";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE)
	 * If the first name attribute is null, this value will be set instead
	 */
	public static final String DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE = "attribute.firstname.nullvalue";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "sn", but you can override it just adding the last
	 * name attribute name you want. "sn" will be the expected field name from
	 * the Response comming from the OpenSaml post call.
	 */
	public static final String DOT_SAML_LASTNAME_ATTRIBUTE = "attribute.lastname.name";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE)
	 * If the last name attribute is null, this value will be set instead
	 */
	public static final String DOT_SAML_LASTNAME_ATTRIBUTE_NULL_VALUE = "attribute.lastname.nullvalue";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: "authorisations", but you can override it just
	 * adding the roles attribute name you want. "authorisations" will be the
	 * expected field name from the Response comming from the OpenSaml post
	 * call.
	 */
	public static final String DOT_SAML_ROLES_ATTRIBUTE = "attribute.roles.name";

	/**
	 * Key for dotmarketing-config.properties By default dotcms use:
	 * {@link DefaultInitializer} but you can override by adding a full class
	 * name to this property.
	 */
	public static final String DOT_SAML_INITIALIZER_CLASS_NAME = "initializer.classname";

	/**
	 * By default we use the {@link OpenSamlAuthenticationServiceImpl}, however
	 * if you want to create or customize your own it is possible by
	 * implementing {@link SamlAuthenticationService} or just extending
	 * {@link OpenSamlAuthenticationServiceImpl} If you need to override it,
	 * just set the classname with this property
	 */
	public static final String DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME = "authentication.service.classname";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use:
	 * {@link com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration} but
	 * you can override by addding a full class name to this property.
	 */
	public static final String DOT_SAML_CONFIGURATION_CLASS_NAME = "configuration.classname";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * In case you have a idp-metadata.xml you can get it from the classpath or
	 * file system. For the classpath you overrides the property with the right
	 * path in your class path. If you want to get the XML from the file system
	 * use the prefix; file://
	 */
	public static final String DOTCMS_SAML_IDP_METADATA_PATH = "idp.metadata.path";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dot cms use
	 * {@link DotSamlConstants}.DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE, in
	 * case you need to use a differente feel free to override this property.
	 */
	public static final String DOT_SAML_IDP_METADATA_PROTOCOL = "idp.metadata.protocol";

	/**
	 * Default value for the metadata protocol see
	 * {@link DotSamlConstants}.DOT_SAML_IDP_METADATA_PROTOCOL
	 */
	public static final String DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE = "urn:oasis:names:tc:SAML:2.0:protocol";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default {@link DefaultMetaDescriptorServiceImpl} is what we use to
	 * parse the idp metadata XML file however if you have you own
	 * implementation of {@link MetaDescriptorService} you can override it.
	 */
	public static final String DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME = "idp.metadata.parser.classname";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we do not filter anything, but if there is some special cases
	 * (url's) you want to avoid the authentication check, add here the values
	 * comma separated.
	 */
	public static final String DOT_SAML_ACCESS_FILTER_VALUES = "access.filter.values";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we include /c and /admin, if you need to add more into the
	 * saml filter you can include the values comma separated.
	 */
	public static final String DOT_SAML_INCLUDE_PATH_VALUES = "include.path.values";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we include "/c/portal/logout,/dotCMS/logout", if you need to
	 * add more into the saml more path you can include the values comma
	 * separated.
	 */
	public static final String DOT_SAML_LOGOUT_PATH_VALUES = "logout.path.values";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default the system will do the verification of the assertion
	 * signature, if for some reason you want to avoid it feel free to set it to
	 * "false".
	 */
	public static final String DOT_SAML_VERIFY_ASSERTION_SIGNATURE = "verify.assertion.signature";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default the system will do the verification of the profile signature,
	 * if for some reason you want to avoid it feel free to set it to "false".
	 */
	public static final String DOT_SAML_VERIFY_SIGNATURE_PROFILE = "verify.signature.profile";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default the system will do the verification of the signature
	 * credentials, if for some reason you want to avoid it feel free to set it
	 * to "false".
	 */
	public static final String DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS = "verify.signature.credentials";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * In case you need a custom credentials for the Service Provider (DotCMS)
	 * overrides the implementation class on the configuration properties.
	 */
	public static final String DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME = "service.provider.custom.credential.provider.classname";

	/**
	 * Protocol used as default in case the DOTCMS_SAML_SERVICE_PROVIDER_ISSUER
	 * is not defined
	 */
	public static final String DOT_SAML_DEFAULT_SERVICE_PROVIDER_PROTOCOL = "service.provider.protocol";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * In case you need a custom credentials for the ID Provider (DotCMS)
	 * overrides the implementation class on the configuration properties.
	 */
	public static final String DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME = "id.provider.custom.credential.provider.classname";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default true, overrides if you want the assertions signed.
	 */
	public static final String DOTCMS_SAML_WANT_ASSERTIONS_SIGNED = "want.assertions.signed";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default true, overrides if you want the authorization requests signed.
	 */
	public static final String DOTCMS_SAML_AUTHN_REQUESTS_SIGNED = "authn.requests.signed";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we use:
	 * "{@link DotSamlConstants}.DOT_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE",
	 * you can override the path to mapping the metadata with whatever value you
	 * need.
	 */
	public static final String DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH = "service.provider.custom.metadata.path";

	/**
	 * Default value "/dotsaml3sp/metadata.xml" for the
	 * {@link DotSamlConstants}.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH
	 * This is the path you have to use in the metadata providers on the open
	 * saml idp (for instance shibboleth) or you can override by using
	 * {@link DotSamlConstants}.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH
	 */
	public static final String DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE = "/dotsaml/metadata.xml";

	/**
	 * Key for dotmarketing-config.properties This optional property defines the
	 * default host used as a fallback to get its SAML configuration, in case a
	 * host does not have any
	 */
	public static final String DOTCMS_SAML_FALLBACK_SITE = "saml.fallback.site";

	/**
	 * Host field that contains the SAML configuration
	 */
	public static final String DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME = "SAML Configuration";

	/**
	 * Host field that enables or disables the current saml configuration for
	 * the host or use the default configuration instead
	 */
	public static final String DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_NAME = "SAML Authentication";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we use: urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect
	 * {@link BindingType}.REDIRECT But if you want to use a diff mechanism from
	 * the Single Sign On Service (see SingleSignOnService tag on the
	 * idp-metadata) please override it
	 *
	 * @see BindingType
	 */
	public static final String DOTCMS_SAML_BINDING_TYPE = "bindingtype";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default false, you can override as a true if you want to force the
	 * authentication.
	 */
	public static final String DOTCMS_SAML_FORCE_AUTHN = "force.authn";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default true, you can override as a false if your assertions are
	 * returned non-encrypted.
	 */
	public static final String DOTCMS_SAML_IS_ASSERTION_ENCRYPTED = "isassertion.encrypted";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we use the implementation
	 * {@link com.dotcms.plugin.saml.v3.handler.HttpPostAssertionResolverHandlerImpl}
	 * which is in charge of resolve the assertion using the SOAP artifact
	 * resolver based on the artifact id pass by the request. If you want a
	 * different implementation please override with the class here.
	 */
	public static final String DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME = "assertion.resolver.handler.classname";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default the plugin checks if the assertion is signed, if for some
	 * reason you IdP sends an unsigned messages, you can set this property as
	 * false. However keep in mind that signed responses are a desire practice.
	 */
	public static final String DOTCMS_SAML_CHECKIF_ASSERTION_SIGNED = "checkif.assertionsigned";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * This is an array comma separated, if this array is set. Any role from
	 * SAML that does not match with the list of include roles pattern, will be
	 * filtered.
	 */
	public static final String DOTCMS_SAML_INCLUDE_ROLES_PATTERN = "include.roles.pattern";

	/**
	 * Optional key set on host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * Role to be assigned to a logged user besides the default SAML User
	 */
	public static final String DOTCMS_SAML_OPTIONAL_USER_ROLE = "role.extra";

	/**
	 * Optional key to configure the strategy to sync the roles from IDP to
	 * DOTCMS Remove user from all roles, add to roles from IdP & saml_user_role
	 * (if set) DOTCMS_SAML_BUILD_ROLES_ALL_VALUE = " all"; Remove user from all
	 * roles, add to roles from IdP DOTCMS_SAML_BUILD_ROLES_IDP_VALUE = "idp";
	 * Remove user from all roles, add to roles from saml_user_role (if set).
	 * Ignore roles from IdP. DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE =
	 * "staticonly; Do not alter existing user roles, add to roles from
	 * saml_user_role (if set). Ignore roles from IdP.
	 * DOTCMS_SAML_BUILD_ROLES_STATIC_ADD_VALUE = "staticadd;
	 * DOTCMS_SAML_BUILD_ROLES_NONE_VALUE Do not alter user roles in any way
	 */
	public static final String DOTCMS_SAML_BUILD_ROLES = "build.roles";

	public static final String DOTCMS_SAML_BUILD_ROLES_ALL_VALUE = "all";
	public static final String DOTCMS_SAML_BUILD_ROLES_IDP_VALUE = "idp";
	public static final String DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE = "staticonly";
	public static final String DOTCMS_SAML_BUILD_ROLES_STATIC_ADD_VALUE = "staticadd";
	public static final String DOTCMS_SAML_BUILD_ROLES_NONE_VALUE = "none";

	/**
	 * Default SAML User role
	 */
	public static final String DOTCMS_SAML_USER_ROLE = "SAML User";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * Default value for text_area field
	 */
	public static final String DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT = "idp.metadata.path=\n" + "keystore.path=\n" + "keystore.password=\n";

	public static final String DEFAULT_SAML_CONFIG_FILE_NAME = "dotcms-saml-default.properties";

	/**
	 * By default we do not include the encryptor in the metadata, if you want
	 * to include it set this to true.
	 */
	public static final String DOTCMS_SAML_USE_ENCRYPTED_DESCRIPTOR = "use.encrypted.descriptor";

	/**
	 * By default the app will try to logout on any site, however you can
	 * override this property per site in order to avoid the plugin to handle
	 * the logout.
	 */
	public static final String DOTCMS_SAML_IS_LOGOUT_NEED = "islogoutneed";

	/**
	 * Initial delay (10 seconds per default) to start the updater task: time is
	 * in seconds
	 */
	public static final String SCHEDULE_UPDATER_TASK_INITIAL_DELAY = "schedule.updater.task.initial.delay";

	/**
	 * Delay between calls to rerun the updater task, by default 10 seconds:
	 * time is in seconds
	 */
	public static final String SCHEDULE_UPDATER_TASK_DELAY_SECONDS = "schedule.updater.task.delay";
}
