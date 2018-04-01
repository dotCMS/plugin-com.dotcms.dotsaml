package com.dotcms.plugin.saml.v4;

import com.dotcms.plugin.saml.v4.BindingType;
import com.dotcms.plugin.saml.v4.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v4.meta.MetaDescriptorService;

/**
 * Encapsulates constants for the dot SAML SP
 *
 * @author jsanca
 */
public class DotSamlConstants
{
	public static final char ARRAY_SEPARATOR_CHAR = ',';
	public static final String HTTP_SCHEMA = "http://";
	public static final String HTTPS_SCHEMA = "https://";
	public static final String HTTPS_SCHEMA_PREFIX = "https";
	public static final String ASSERTION_CONSUMER_ENDPOINT_DOTSAML4SP = "/dotsaml/login";
	public static final String LOGOUT_SERVICE_ENDPOINT_DOTSAML4SP = "/dotsaml/logout";
	public static final String RESPONSE_AND_ASSERTION = "responseandassertion";
	public static final String RESPONSE = "response";
	public static final String ASSERTION = "assertion";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v4.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * This is the customer endpoint url, which means the url where to be
	 * redirected when the user gets log back to dotcms. You can set it for
	 * instance to http://[domain]/c in order to get back to the landing page,
	 * dotCMS will redirect as soon as do the artificial login with the SAML
	 * information, to the original request.
	 */
	public static final String DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL = "assertion.customer.endpoint.url";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
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
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotCMS uses:
	 * {@link org.opensaml.saml.saml2.core.AuthnContext}.PASSWORD_AUTHN_CTX, but
	 * you can override it just adding the context class ref you want.
	 */
	public static final String DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF = "authn.context.class.ref";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v4.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default true, overrides if you want the authorization requests signed.
	 */
	public static final String DOTCMS_SAML_AUTHN_REQUESTS_SIGNED = "authn.requests.signed";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v4.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
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
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default the plugin checks if the assertion is signed, if for some
	 * reason you IdP sends an unsigned messages, you can set this property as
	 * false. However keep in mind that signed responses are a desire practice.
	 */
	public static final String DOTCMS_SAML_CHECKIF_ASSERTION_SIGNED = "checkif.assertionsigned";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: 1000, but you can override it just adding the new
	 * time you want.
	 */
	public static final String DOT_SAML_CLOCK_SKEW = "clock.skew";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v4.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * This is the logout service endpoint url, which means the url where to be
	 * redirected when the user gets log out. You can set it for instance to
	 * http://[domain]/c in order to get back to the page.
	 */
	public static final String DOT_SAML_LOGOUT_SERVICE_ENDPOINT_URL = "logout.service.endpoint.url";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default {@link DefaultMetaDescriptorServiceImpl} is what we use to
	 * parse the idp metadata XML file however if you have you own
	 * implementation of {@link MetaDescriptorService} you can override it.
	 */
	public static final String DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME = "idp.metadata.parser.classname";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
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
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default true, you can override as a false if your assertions are
	 * returned non-encrypted.
	 */
	public static final String DOTCMS_SAML_IS_ASSERTION_ENCRYPTED = "isassertion.encrypted";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default dotcms use: 2000, but you can override it just adding the new
	 * time you want.
	 */
	public static final String DOT_SAML_MESSAGE_LIFE_TIME = "message.life.time";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default we use the implementation
	 * {@link handler.HttpPostAssertionResolverHandlerImpl}
	 * which is in charge of resolve the assertion using the SOAP artifact
	 * resolver based on the artifact id pass by the request. If you want a
	 * different implementation please override with the class here.
	 */
	public static final String DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME = "assertion.resolver.handler.classname";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default false, you can override as a true if you want to force the
	 * authentication.
	 */
	public static final String DOTCMS_SAML_FORCE_AUTHN = "force.authn";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * In case you need a custom credentials for the ID Provider (DotCMS)
	 * overrides the implementation class on the configuration properties.
	 */
	public static final String DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME = "id.provider.custom.credential.provider.classname";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * If you have already set a idp-metadata, this value will be taken from it,
	 * otherwise you have to set it on the dotCMS properties. If the value is
	 * not present will got an exception on runtime. This value is the Redirect
	 * SLO (Logout) url (usually Shibboleth), which is the one to be redirect
	 * when the user does logout on dotCMS.
	 */
	public static final String DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SLO_URL = "identity.provider.destinationslo.url";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * If you have already set a idp-metadata, this value will be taken from it,
	 * otherwise you have to set it on the dotCMS properties. If the value is
	 * not present will got an exception on runtime. This value is the Redirect
	 * SSO url (usually Shibboleth), which is the one to be redirect when the
	 * user is not logged on dotCMS.
	 */
	public static final String DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL = "identity.provider.destinationsso.url";

	/**
	 * SAML allows several formats, such as Kerberos, email, Windows Domain
	 * Qualified Name, etc. By default dotcms use:
	 * {@link org.opensaml.saml.saml2.core.NameIDType}.TRANSIENT. See More on
	 * {@link org.opensaml.saml.saml2.core.NameIDType}
	 */
	public static final String DOTCMS_SAML_NAME_ID_POLICY_FORMAT = "nameidpolicy.format";

	/**
	 * If you want to allow to create an user that does not exists on the IdP,
	 * set this to true, otherwise false. By default it is false, so won't allow
	 * to create the user, however the Idp will be the final responsable to
	 * decided if the user could be or not created.
	 */
	public static final String DOTCMS_SAML_POLICY_ALLOW_CREATE = "policy.allowcreate";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * Used to get the Saml protocol binding, by default use
	 * {@link DotSamlConstants}.SAML2_ARTIFACT_BINDING_URI
	 */
	public static final String DOTCMS_SAML_PROTOCOL_BINDING = "protocol.binding";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * In case you need a custom credentials for the Service Provider (DotCMS)
	 * overrides the implementation class on the configuration properties.
	 */
	public static final String DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME = "service.provider.custom.credential.provider.classname";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * For the provider issue which is the identifier for the sender, by default
	 * is: DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_ISSUER_DEFAULT_VALUE
	 * Usually you overrides it to your host domain, for instance our id could
	 * be http://dotcms.com.
	 */
	public static final String DOTCMS_SAML_SERVICE_PROVIDER_ISSUER = "service.provider.issuer";

	/**
	 * By default we do not include the encryptor in the metadata, if you want
	 * to include it set this to true.
	 */
	public static final String DOTCMS_SAML_USE_ENCRYPTED_DESCRIPTOR = "use.encrypted.descriptor";

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
	 * By default the system will do the verification of the signature
	 * credentials, if for some reason you want to avoid it feel free to set it
	 * to "false".
	 */
	public static final String DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS = "verify.signature.credentials";

	/**
	 * Key for host field configuration (see
	 * {@link com.dotcms.plugin.saml.v3.DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default the system will do the verification of the profile signature,
	 * if for some reason you want to avoid it feel free to set it to "false".
	 */
	public static final String DOT_SAML_VERIFY_SIGNATURE_PROFILE = "verify.signature.profile";

	/**
	 * Key for host field configuration (see
	 * {@link DotSamlConstants}.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME)
	 * By default true, overrides if you want the assertions signed.
	 */
	public static final String DOTCMS_SAML_WANT_ASSERTIONS_SIGNED = "want.assertions.signed";
}
