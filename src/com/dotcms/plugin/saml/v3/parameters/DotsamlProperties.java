package com.dotcms.plugin.saml.v3.parameters;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.NameIDType;

import com.dotcms.plugin.saml.v3.key.BindingType;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import org.apache.commons.lang.StringUtils;

public class DotsamlProperties {

	private String dotSamlAccessFilterValues = null;
	private String dotcmsSamlAuthnComparisonType = null;
	private String dotcmsSamlAuthnContextClassRef = AuthnContext.PASSWORD_AUTHN_CTX;
	private String dotcmsSamlBindingType = BindingType.REDIRECT.getBinding();
	private String dotcmsSamlBuildRoles = DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_ALL_VALUE;
	private Integer dotSamlClockSkew = 10000;
	private String dotSamlEmailAttribute = "mail";
	private Boolean dotSamlEmailAttributeNullValue = true;
	private String dotSamlFirstnameAttribute = "givenName";
	private String dotSamlFirstnameAttributeNullValue = null;
	private String dotSamlLastnameAttribute = "sn";
	private String dotSamlLastnameAttributeNullValue = null;
	private String dotSamlRolesAttribute = "authorizations";
	private Boolean dotcmsSamlIsLogoutNeeded = true;
	private String dotSamlLogoutServiceEndpointUrl = null;
	private String dotSamlIdpMetadataParserClassName = null;
	private String dotSamlIdpMetadataProtocol = DotSamlConstants.DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE;
	private Boolean dotcmsSamlIsAssertionEncrypted = false;
	private Integer dotSamlMessageLifeTime = 20000;
	private String dotcmsSamlAssertionResolverHandlerClassName = null;
	private Boolean dotcmsSamlForceAuthn = false;
	private String dotSamlIdProviderCustomCredentialProviderClassname = null;
	private String dotcmsSamlIdentityProviderDestinationSloUrl = null;
	private String dotcmsSamlIdentityProviderDestinationSsoUrl = null;
	private String dotSamlIncludePathValues = DotSamlConstants.DOT_SAML_INCLUDE_PATH_DEFAULT_VALUES;
	private String dotcmsSamlIncludeRolesPattern = null;
	private String dotSamlLogoutPathValues = DotSamlConstants.DOT_SAML_LOGOUT_PATH_DEFAULT_VALUES;
	private String dotcmsSamlNameIdPolicyFormat = NameIDType.PERSISTENT;
	private String dotcmsSamlOptionalUserRole = null;
	private Boolean dotcmsSamlPolicyAllowCreate = false;
	private String dotcmsSamlProtocolBinding = SAMLConstants.SAML2_REDIRECT_BINDING_URI;
	private String dotSamlRemoveRolesPrefix = StringUtils.EMPTY;
	private String dotSamlServiceProviderCustomCredentialProviderClassname = null;
	private Boolean dotcmsSamlUseEncryptedDescriptor = false;
	private Boolean dotSamlVerifySignatureCredentials = true;
	private Boolean dotSamlVerifySignatureProfile = true;
	private Boolean dotcmsSamlClearLocationQueryParams = true;
	private Boolean dotcmsSamlLoginEmailUpdate = true;

	public String getDotSamlAccessFilterValues() {
		return dotSamlAccessFilterValues;
	}

	public void setDotSamlAccessFilterValues(String dotSamlAccessFilterValues) {
		this.dotSamlAccessFilterValues = dotSamlAccessFilterValues;
	}

	public String getDotcmsSamlAuthnComparisonType() {
		return dotcmsSamlAuthnComparisonType;
	}

	public void setDotcmsSamlAuthnComparisonType(String dotcmsSamlAuthnComparisonType) {
		this.dotcmsSamlAuthnComparisonType = dotcmsSamlAuthnComparisonType;
	}

	public String getDotcmsSamlAuthnContextClassRef() {
		return dotcmsSamlAuthnContextClassRef;
	}

	public void setDotcmsSamlAuthnContextClassRef(String dotcmsSamlAuthnContextClassRef) {
		this.dotcmsSamlAuthnContextClassRef = dotcmsSamlAuthnContextClassRef;
	}

	public String getDotcmsSamlBindingType() {
		return dotcmsSamlBindingType;
	}

	public void setDotcmsSamlBindingType(String dotcmsSamlBindingType) {
		this.dotcmsSamlBindingType = dotcmsSamlBindingType;
	}

	public String getDotcmsSamlBuildRoles() {
		return dotcmsSamlBuildRoles;
	}

	public void setDotcmsSamlBuildRoles(String dotcmsSamlBuildRoles) {
		this.dotcmsSamlBuildRoles = dotcmsSamlBuildRoles;
	}

	public Integer getDotSamlClockSkew() {
		return dotSamlClockSkew;
	}

	public void setDotSamlClockSkew(Integer dotSamlClockSkew) {
		this.dotSamlClockSkew = dotSamlClockSkew;
	}

	public String getDotSamlEmailAttribute() {
		return dotSamlEmailAttribute;
	}

	public void setDotSamlEmailAttribute(String dotSamlEmailAttribute) {
		this.dotSamlEmailAttribute = dotSamlEmailAttribute;
	}

	public Boolean isDotSamlEmailAttributeNullValue() {
		return dotSamlEmailAttributeNullValue;
	}

	public void setDotSamlEmailAttributeNullValue(Boolean dotSamlEmailAttributeNullValue) {
		this.dotSamlEmailAttributeNullValue = dotSamlEmailAttributeNullValue;
	}

	public String getDotSamlFirstnameAttribute() {
		return dotSamlFirstnameAttribute;
	}

	public void setDotSamlFirstnameAttribute(String dotSamlFirstnameAttribute) {
		this.dotSamlFirstnameAttribute = dotSamlFirstnameAttribute;
	}

	public String getDotSamlFirstnameAttributeNullValue() {
		return dotSamlFirstnameAttributeNullValue;
	}

	public void setDotSamlFirstnameAttributeNullValue(String dotSamlFirstnameAttributeNullValue) {
		this.dotSamlFirstnameAttributeNullValue = dotSamlFirstnameAttributeNullValue;
	}

	public String getDotSamlLastnameAttribute() {
		return dotSamlLastnameAttribute;
	}

	public void setDotSamlLastnameAttribute(String dotSamlLastnameAttribute) {
		this.dotSamlLastnameAttribute = dotSamlLastnameAttribute;
	}

	public String getDotSamlLastnameAttributeNullValue() {
		return dotSamlLastnameAttributeNullValue;
	}

	public void setDotSamlLastnameAttributeNullValue(String dotSamlLastnameAttributeNullValue) {
		this.dotSamlLastnameAttributeNullValue = dotSamlLastnameAttributeNullValue;
	}

	public String getDotSamlRolesAttribute() {
		return dotSamlRolesAttribute;
	}

	public void setDotSamlRolesAttribute(String dotSamlRolesAttribute) {
		this.dotSamlRolesAttribute = dotSamlRolesAttribute;
	}

	public Boolean isDotcmsSamlIsLogoutNeeded() {
		return dotcmsSamlIsLogoutNeeded;
	}

	public void setDotcmsSamlIsLogoutNeeded(Boolean dotcmsSamlIsLogoutNeeded) {
		this.dotcmsSamlIsLogoutNeeded = dotcmsSamlIsLogoutNeeded;
	}

	public String getDotSamlLogoutServiceEndpointUrl() {
		return dotSamlLogoutServiceEndpointUrl;
	}

	public void setDotSamlLogoutServiceEndpointUrl(String dotSamlLogoutServiceEndpointUrl) {
		this.dotSamlLogoutServiceEndpointUrl = dotSamlLogoutServiceEndpointUrl;
	}

	public String getDotSamlIdpMetadataParserClassName() {
		return dotSamlIdpMetadataParserClassName;
	}

	public void setDotSamlIdpMetadataParserClassName(String dotSamlIdpMetadataParserClassName) {
		this.dotSamlIdpMetadataParserClassName = dotSamlIdpMetadataParserClassName;
	}

	public String getDotSamlIdpMetadataProtocol() {
		return dotSamlIdpMetadataProtocol;
	}

	public void setDotSamlIdpMetadataProtocol(String dotSamlIdpMetadataProtocol) {
		this.dotSamlIdpMetadataProtocol = dotSamlIdpMetadataProtocol;
	}

	public Boolean isDotcmsSamlIsAssertionEncrypted() {
		return dotcmsSamlIsAssertionEncrypted;
	}

	public void setDotcmsSamlIsAssertionEncrypted(Boolean dotcmsSamlIsAssertionEncrypted) {
		this.dotcmsSamlIsAssertionEncrypted = dotcmsSamlIsAssertionEncrypted;
	}

	public Integer getDotSamlMessageLifeTime() {
		return dotSamlMessageLifeTime;
	}

	public void setDotSamlMessageLifeTime(Integer dotSamlMessageLifeTime) {
		this.dotSamlMessageLifeTime = dotSamlMessageLifeTime;
	}

	public String getDotcmsSamlAssertionResolverHandlerClassName() {
		return dotcmsSamlAssertionResolverHandlerClassName;
	}

	public void setDotcmsSamlAssertionResolverHandlerClassName(String dotcmsSamlAssertionResolverHandlerClassName) {
		this.dotcmsSamlAssertionResolverHandlerClassName = dotcmsSamlAssertionResolverHandlerClassName;
	}

	public Boolean isDotcmsSamlForceAuthn() {
		return dotcmsSamlForceAuthn;
	}

	public void setDotcmsSamlForceAuthn(Boolean dotcmsSamlForceAuthn) {
		this.dotcmsSamlForceAuthn = dotcmsSamlForceAuthn;
	}

	public String getDotSamlIdProviderCustomCredentialProviderClassname() {
		return dotSamlIdProviderCustomCredentialProviderClassname;
	}

	public void setDotSamlIdProviderCustomCredentialProviderClassname(
			String dotSamlIdProviderCustomCredentialProviderClassname) {
		this.dotSamlIdProviderCustomCredentialProviderClassname = dotSamlIdProviderCustomCredentialProviderClassname;
	}

	public String getDotcmsSamlIdentityProviderDestinationSloUrl() {
		return dotcmsSamlIdentityProviderDestinationSloUrl;
	}

	public void setDotcmsSamlIdentityProviderDestinationSloUrl(String dotcmsSamlIdentityProviderDestinationSloUrl) {
		this.dotcmsSamlIdentityProviderDestinationSloUrl = dotcmsSamlIdentityProviderDestinationSloUrl;
	}

	public String getDotcmsSamlIdentityProviderDestinationSsoUrl() {
		return dotcmsSamlIdentityProviderDestinationSsoUrl;
	}

	public void setDotcmsSamlIdentityProviderDestinationSsoUrl(String dotcmsSamlIdentityProviderDestinationSsoUrl) {
		this.dotcmsSamlIdentityProviderDestinationSsoUrl = dotcmsSamlIdentityProviderDestinationSsoUrl;
	}

	public String getDotSamlIncludePathValues() {
		return dotSamlIncludePathValues;
	}

	public void setDotSamlIncludePathValues(String dotSamlIncludePathValues) {
		this.dotSamlIncludePathValues = dotSamlIncludePathValues;
	}

	public String getDotcmsSamlIncludeRolesPattern() {
		return dotcmsSamlIncludeRolesPattern;
	}

	public void setDotcmsSamlIncludeRolesPattern(String dotcmsSamlIncludeRolesPattern) {
		this.dotcmsSamlIncludeRolesPattern = dotcmsSamlIncludeRolesPattern;
	}

	public String getDotSamlLogoutPathValues() {
		return dotSamlLogoutPathValues;
	}

	public void setDotSamlLogoutPathValues(String dotSamlLogoutPathValues) {
		this.dotSamlLogoutPathValues = dotSamlLogoutPathValues;
	}

	public String getDotcmsSamlNameIdPolicyFormat() {
		return dotcmsSamlNameIdPolicyFormat;
	}

	public void setDotcmsSamlNameIdPolicyFormat(String dotcmsSamlNameIdPolicyFormat) {
		this.dotcmsSamlNameIdPolicyFormat = dotcmsSamlNameIdPolicyFormat;
	}

	public String getDotcmsSamlOptionalUserRole() {
		return dotcmsSamlOptionalUserRole;
	}

	public void setDotcmsSamlOptionalUserRole(String dotcmsSamlOptionalUserRole) {
		this.dotcmsSamlOptionalUserRole = dotcmsSamlOptionalUserRole;
	}

	public Boolean isDotcmsSamlPolicyAllowCreate() {
		return dotcmsSamlPolicyAllowCreate;
	}

	public void setDotcmsSamlPolicyAllowCreate(Boolean dotcmsSamlPolicyAllowCreate) {
		this.dotcmsSamlPolicyAllowCreate = dotcmsSamlPolicyAllowCreate;
	}

	public String getDotcmsSamlProtocolBinding() {
		return dotcmsSamlProtocolBinding;
	}

	public void setDotcmsSamlProtocolBinding(String dotcmsSamlProtocolBinding) {
		this.dotcmsSamlProtocolBinding = dotcmsSamlProtocolBinding;
	}

	public String getDotSamlRemoveRolesPrefix() {
		return dotSamlRemoveRolesPrefix;
	}

	public void setDotSamlRemoveRolesPrefix(String dotSamlRemoveRolesPrefix) {
		this.dotSamlRemoveRolesPrefix = dotSamlRemoveRolesPrefix;
	}

	public String getDotSamlServiceProviderCustomCredentialProviderClassname() {
		return dotSamlServiceProviderCustomCredentialProviderClassname;
	}

	public void setDotSamlServiceProviderCustomCredentialProviderClassname(
			String dotSamlServiceProviderCustomCredentialProviderClassname) {
		this.dotSamlServiceProviderCustomCredentialProviderClassname = dotSamlServiceProviderCustomCredentialProviderClassname;
	}

	public Boolean isDotcmsSamlUseEncryptedDescriptor() {
		return dotcmsSamlUseEncryptedDescriptor;
	}

	public void setDotcmsSamlUseEncryptedDescriptor(Boolean dotcmsSamlUseEncryptedDescriptor) {
		this.dotcmsSamlUseEncryptedDescriptor = dotcmsSamlUseEncryptedDescriptor;
	}

	public Boolean isDotSamlVerifySignatureCredentials() {
		return dotSamlVerifySignatureCredentials;
	}

	public void setDotSamlVerifySignatureCredentials(Boolean dotSamlVerifySignatureCredentials) {
		this.dotSamlVerifySignatureCredentials = dotSamlVerifySignatureCredentials;
	}

	public Boolean isDotSamlVerifySignatureProfile() {
		return dotSamlVerifySignatureProfile;
	}

	public void setDotSamlVerifySignatureProfile(Boolean dotSamlVerifySignatureProfile) {
		this.dotSamlVerifySignatureProfile = dotSamlVerifySignatureProfile;
	}

	public Boolean isDotcmsSamlClearLocationQueryParams() {
		return dotcmsSamlClearLocationQueryParams;
	}

	public void setDotcmsSamlClearLocationQueryParams(Boolean dotcmsSamlClearLocationQueryParams) {
		this.dotcmsSamlClearLocationQueryParams = dotcmsSamlClearLocationQueryParams;
	}

	public Boolean isDotcmsSamlLoginEmailUpdate() {
		return dotcmsSamlLoginEmailUpdate;
	}

	public void setDotcmsSamlLoginEmailUpdate(Boolean dotcmsSamlLoginEmailUpdate) {
		this.dotcmsSamlLoginEmailUpdate = dotcmsSamlLoginEmailUpdate;
	}
}