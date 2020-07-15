package com.dotcms.plugin.saml.v3.parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.liferay.util.FileUtil;

public class DotsamlDefaultPropertiesService {

	public static final String INTEGER_PARSE_ERROR = "Unable to parse Integer value: ";
	public static final String NOT_FOUND_ERROR = "Property Name not Found: ";
	public static final String UNABLE_TO_READ_FILE = "File does not exist or unable to read : ";
	public static final String UNABLE_TO_CLOSE_FILE = "Unable to close file : ";
	public static final String PROPERTIES_PATH = File.separator + "saml" + File.separator
			+ "dotcms-saml-default.properties";

	private static final String assetsPath = Config.getStringProperty("ASSET_REAL_PATH",
			FileUtil.getRealPath(Config.getStringProperty("ASSET_PATH", "/assets")));;
	private static final String idpFilePath = assetsPath + PROPERTIES_PATH;
	private static final DotsamlProperties defaultParams = new DotsamlProperties();

	public static void init() {
		updateDefaultParameters(loadOptionalDefaults());
	}

	private static Properties loadOptionalDefaults() {
		Properties props = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(idpFilePath);
			props.load(input);

		} catch (IOException ex) {
			// Since this is optional, it is valid to not have the file.
			// Log and go on.
			Logger.warn(DotsamlDefaultPropertiesService.class, UNABLE_TO_READ_FILE + idpFilePath);

		} finally {
			if (input != null) {

				try {
					input.close();
				} catch (IOException e) {
					Logger.error(DotsamlDefaultPropertiesService.class, UNABLE_TO_CLOSE_FILE + idpFilePath);
					e.printStackTrace();
				}
			}
		}

		return props;
	}

	public static DotsamlProperties getDefaultParams() {
		return defaultParams;
	}

	public static void updateDefaultParameters(Properties props) {

		if (props == null) {
			return;
		}

		props.forEach((key, value) -> {

			updateDefaultParameter(DotsamlPropertyName.findProperty((String) key), (String) value);
		});
	}

	public static void updateDefaultParameter(DotsamlPropertyName property, String value) {

		if (property == null) {
			Logger.warn(DotsamlDefaultPropertiesService.class, "updateDefaultParameter: property is null!");
			return;
		}

		if (value == null) {
			Logger.warn(DotsamlDefaultPropertiesService.class, "updateDefaultParameter: value is null!");
			return;
		}

		Logger.info(DotsamlDefaultPropertiesService.class, "Updating default property : "
				+ property.getPropertyName() + " - " + value );
		
		switch (property) {

		case DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME:
			defaultParams.setDotcmsSamlAssertionResolverHandlerClassName(value);
			break;
		case DOTCMS_SAML_AUTHN_COMPARISON_TYPE:
			defaultParams.setDotcmsSamlAuthnComparisonType(value);
			break;
		case DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF:
			defaultParams.setDotcmsSamlAuthnContextClassRef(value);
			break;
		case DOTCMS_SAML_BINDING_TYPE:
			defaultParams.setDotcmsSamlBindingType(value);
			break;
		case DOTCMS_SAML_BUILD_ROLES:
			defaultParams.setDotcmsSamlBuildRoles(value);
			break;
		case DOTCMS_SAML_FORCE_AUTHN:
			defaultParams.setDotcmsSamlForceAuthn(Boolean.parseBoolean(value));
			break;
		case DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SLO_URL:
			defaultParams.setDotcmsSamlIdentityProviderDestinationSloUrl(value);
			break;
		case DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL:
			defaultParams.setDotcmsSamlIdentityProviderDestinationSsoUrl(value);
			break;
		case DOTCMS_SAML_INCLUDE_ROLES_PATTERN:
			defaultParams.setDotcmsSamlIncludeRolesPattern(value);
			break;
		case DOTCMS_SAML_IS_ASSERTION_ENCRYPTED:
			defaultParams.setDotcmsSamlIsAssertionEncrypted(Boolean.parseBoolean(value));
			break;
		case DOTCMS_SAML_IS_LOGOUT_NEED:
			defaultParams.setDotcmsSamlIsLogoutNeeded(Boolean.parseBoolean(value));
			break;
		case DOTCMS_SAML_NAME_ID_POLICY_FORMAT:
			defaultParams.setDotcmsSamlNameIdPolicyFormat(value);
			break;
		case DOTCMS_SAML_OPTIONAL_USER_ROLE:
			defaultParams.setDotcmsSamlOptionalUserRole(value);
			break;
		case DOTCMS_SAML_POLICY_ALLOW_CREATE:
			defaultParams.setDotcmsSamlPolicyAllowCreate(Boolean.parseBoolean(value));
			break;
		case DOTCMS_SAML_PROTOCOL_BINDING:
			defaultParams.setDotcmsSamlProtocolBinding(value);
			break;
		case DOTCMS_SAML_USE_ENCRYPTED_DESCRIPTOR:
			defaultParams.setDotcmsSamlUseEncryptedDescriptor(Boolean.parseBoolean(value));
			break;
		case DOT_SAML_ACCESS_FILTER_VALUES:
			defaultParams.setDotSamlAccessFilterValues(value);
			break;
		case DOT_SAML_CLOCK_SKEW:
			try {
				defaultParams.setDotSamlClockSkew(Integer.parseInt(value));
			} catch (Exception ex) {
				Logger.warn(DotsamlDefaultPropertiesService.class,
						INTEGER_PARSE_ERROR + property.getPropertyName() + ":" + value);
			}
			break;
		case DOT_SAML_EMAIL_ATTRIBUTE:
			defaultParams.setDotSamlEmailAttribute(value);
			break;
		case DOT_SAML_EMAIL_ATTRIBUTE_ALLOW_NULL:
			defaultParams.setDotSamlEmailAttributeNullValue(Boolean.parseBoolean(value));
			break;
		case DOT_SAML_FIRSTNAME_ATTRIBUTE:
			defaultParams.setDotSamlFirstnameAttribute(value);
			break;
		case DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE:
			defaultParams.setDotSamlFirstnameAttributeNullValue(value);
			break;
		case DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME:
			defaultParams.setDotSamlIdpMetadataParserClassName(value);
			break;
		case DOT_SAML_IDP_METADATA_PROTOCOL:
			defaultParams.setDotSamlIdpMetadataProtocol(value);
			break;
		case DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME:
			defaultParams.setDotSamlIdProviderCustomCredentialProviderClassname(value);
			break;
		case DOT_SAML_INCLUDE_PATH_VALUES:
			defaultParams.setDotSamlIncludePathValues(value);
			break;
		case DOT_SAML_LASTNAME_ATTRIBUTE:
			defaultParams.setDotSamlLastnameAttribute(value);
			break;
		case DOT_SAML_LASTNAME_ATTRIBUTE_NULL_VALUE:
			defaultParams.setDotSamlLastnameAttributeNullValue(value);
			break;
		case DOT_SAML_LOGOUT_PATH_VALUES:
			defaultParams.setDotSamlLogoutPathValues(value);
			break;
		case DOT_SAML_LOGOUT_SERVICE_ENDPOINT_URL:
			defaultParams.setDotSamlLogoutServiceEndpointUrl(value);
			break;
		case DOT_SAML_MESSAGE_LIFE_TIME:
			try {
				defaultParams.setDotSamlMessageLifeTime(Integer.parseInt(value));
			} catch (Exception ex) {
				Logger.warn(DotsamlDefaultPropertiesService.class,
						INTEGER_PARSE_ERROR + property.getPropertyName() + ":" + value);
			}
			break;
		case DOT_SAML_REMOVE_ROLES_PREFIX:
			defaultParams.setDotSamlRemoveRolesPrefix(value);
			break;
		case DOT_SAML_ROLES_ATTRIBUTE:
			defaultParams.setDotSamlRolesAttribute(value);
			break;
		case DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME:
			defaultParams.setDotSamlServiceProviderCustomCredentialProviderClassname(value);
			break;
		case DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS:
			defaultParams.setDotSamlVerifySignatureCredentials(Boolean.parseBoolean(value));
			break;
		case DOT_SAML_VERIFY_SIGNATURE_PROFILE:
			defaultParams.setDotSamlVerifySignatureProfile(Boolean.parseBoolean(value));
			break;
		case DOTCMS_SAML_CLEAR_LOCATION_QUERY_PARAMS:
			defaultParams.setDotcmsSamlClearLocationQueryParams(Boolean.parseBoolean(value));
			break;
		default:
			Logger.warn(DotsamlDefaultPropertiesService.class,
					NOT_FOUND_ERROR + property.getPropertyName() + ":" + value);
			break;
		}
	}

	public static String getDefaultStringParameter(DotsamlPropertyName property) throws DotSamlException {

		if (property == null) {
			throw new DotSamlException("getDefaultStringParameter: property is null!");
		}

		switch (property) {

		case DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME:
			return defaultParams.getDotcmsSamlAssertionResolverHandlerClassName();
		case DOTCMS_SAML_AUTHN_COMPARISON_TYPE:
			return defaultParams.getDotcmsSamlAuthnComparisonType();
		case DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF:
			return defaultParams.getDotcmsSamlAuthnContextClassRef();
		case DOTCMS_SAML_BINDING_TYPE:
			return defaultParams.getDotcmsSamlBindingType();
		case DOTCMS_SAML_BUILD_ROLES:
			return defaultParams.getDotcmsSamlBuildRoles();
		case DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SLO_URL:
			return defaultParams.getDotcmsSamlIdentityProviderDestinationSloUrl();
		case DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL:
			return defaultParams.getDotcmsSamlIdentityProviderDestinationSsoUrl();
		case DOTCMS_SAML_NAME_ID_POLICY_FORMAT:
			return defaultParams.getDotcmsSamlNameIdPolicyFormat();
		case DOTCMS_SAML_OPTIONAL_USER_ROLE:
			return defaultParams.getDotcmsSamlOptionalUserRole();
		case DOTCMS_SAML_PROTOCOL_BINDING:
			return defaultParams.getDotcmsSamlProtocolBinding();
		case DOT_SAML_ACCESS_FILTER_VALUES:
			return defaultParams.getDotSamlAccessFilterValues();
		case DOT_SAML_EMAIL_ATTRIBUTE:
			return defaultParams.getDotSamlEmailAttribute();
		case DOT_SAML_FIRSTNAME_ATTRIBUTE:
			return defaultParams.getDotSamlFirstnameAttribute();
		case DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME:
			return defaultParams.getDotSamlIdpMetadataParserClassName();
		case DOT_SAML_IDP_METADATA_PROTOCOL:
			return defaultParams.getDotSamlIdpMetadataProtocol();
		case DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME:
			return defaultParams.getDotSamlIdProviderCustomCredentialProviderClassname();
		case DOT_SAML_INCLUDE_PATH_VALUES:
			return defaultParams.getDotSamlIncludePathValues();
		case DOT_SAML_LASTNAME_ATTRIBUTE:
			return defaultParams.getDotSamlLastnameAttribute();
		case DOT_SAML_LOGOUT_PATH_VALUES:
			return defaultParams.getDotSamlLogoutPathValues();
		case DOT_SAML_LOGOUT_SERVICE_ENDPOINT_URL:
			return defaultParams.getDotSamlLogoutServiceEndpointUrl();
		case DOT_SAML_REMOVE_ROLES_PREFIX:
			return defaultParams.getDotSamlRemoveRolesPrefix();
		case DOT_SAML_ROLES_ATTRIBUTE:
			return defaultParams.getDotSamlRolesAttribute();
		case DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME:
			return defaultParams.getDotSamlServiceProviderCustomCredentialProviderClassname();
		case DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE:
			return defaultParams.getDotSamlFirstnameAttributeNullValue();
		case DOT_SAML_LASTNAME_ATTRIBUTE_NULL_VALUE:
			return defaultParams.getDotSamlLastnameAttributeNullValue();
		case DOTCMS_SAML_INCLUDE_ROLES_PATTERN:
			return defaultParams.getDotcmsSamlIncludeRolesPattern();
		default:
			break;
		}

		throw new DotSamlException(NOT_FOUND_ERROR + property.getPropertyName());
	}

	public static boolean getDefaultBooleanParameter(DotsamlPropertyName property) throws DotSamlException {

		if (property == null) {
			throw new DotSamlException("getDefaultBooleanParameter: property is null!");
		}

		switch (property) {

		case DOTCMS_SAML_FORCE_AUTHN:
			return defaultParams.isDotcmsSamlForceAuthn();
		case DOTCMS_SAML_IS_ASSERTION_ENCRYPTED:
			return defaultParams.isDotcmsSamlIsAssertionEncrypted();
		case DOTCMS_SAML_IS_LOGOUT_NEED:
			return defaultParams.isDotcmsSamlIsLogoutNeeded();
		case DOTCMS_SAML_POLICY_ALLOW_CREATE:
			return defaultParams.isDotcmsSamlPolicyAllowCreate();
		case DOTCMS_SAML_USE_ENCRYPTED_DESCRIPTOR:
			return defaultParams.isDotcmsSamlUseEncryptedDescriptor();
		case DOT_SAML_EMAIL_ATTRIBUTE_ALLOW_NULL:
			return defaultParams.isDotSamlEmailAttributeNullValue();
		case DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS:
			return defaultParams.isDotSamlVerifySignatureCredentials();
		case DOT_SAML_VERIFY_SIGNATURE_PROFILE:
			return defaultParams.isDotSamlVerifySignatureProfile();
		case DOTCMS_SAML_CLEAR_LOCATION_QUERY_PARAMS:
				return defaultParams.isDotcmsSamlClearLocationQueryParams();
		default:
			break;
		}

		throw new DotSamlException(NOT_FOUND_ERROR + property.getPropertyName());
	}

	public static Integer getDefaultIntegerParameter(DotsamlPropertyName property) throws DotSamlException {

		if (property == null) {
			throw new DotSamlException("getDefaultIntegerParameter: property is null!");
		}

		switch (property) {

		case DOT_SAML_CLOCK_SKEW:
			return defaultParams.getDotSamlClockSkew();
		case DOT_SAML_MESSAGE_LIFE_TIME:
			return defaultParams.getDotSamlMessageLifeTime();
		default:
			break;
		}

		throw new DotSamlException(NOT_FOUND_ERROR + property.getPropertyName());
	}

}
