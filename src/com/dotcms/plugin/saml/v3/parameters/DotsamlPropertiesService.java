package com.dotcms.plugin.saml.v3.parameters;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import org.apache.commons.lang.StringUtils;
import com.dotmarketing.util.Logger;

/**
 * Provides a helper to optional properties of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
// todo: partially migrated
public class DotsamlPropertiesService {

	/**
	 * Get an optional property value from the idpConfig, if it does not exist
	 * returns system defaultValue
	 * 
	 * @param idpConfig
	 *            IdpConfig
	 * @param propertyName
	 *            String
	 * @return boolean
	 */
	public static Boolean getOptionBoolean(IdpConfig idpConfig, DotsamlPropertyName propertyName) {

		Boolean value = null;

		try {

			if (idpConfig.getOptionalProperties().containsKey(propertyName.getPropertyName())) {

				String property = (String) idpConfig.getOptionalProperties().get(propertyName.getPropertyName());
				value = Boolean.parseBoolean(property);

			} else {

				value = DotsamlDefaultPropertiesService.getDefaultBooleanParameter(propertyName);
			}

			Logger.debug(DotsamlPropertiesService.class,
					"Found " + propertyName.getPropertyName() + " : " + ((value == null) ? "null" : value));

		} catch (Exception e) {

			Logger.warn(DotsamlPropertiesService.class, "Cast exception on " + propertyName.getPropertyName()
					+ " property. idpConfigId: " + idpConfig.getId());
		}

		return value;
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist
	 * returns null
	 * 
	 * @param idpConfig
	 *            IdpConfig
	 * @param propertyName
	 *            String
	 * @return String
	 */
	public static String getOptionString(IdpConfig idpConfig, DotsamlPropertyName propertyName) {
		String value = null;

		try {
			if (idpConfig.getOptionalProperties().containsKey(propertyName.getPropertyName())) {

				value = (String) idpConfig.getOptionalProperties().get(propertyName.getPropertyName());

			} else {

				value = DotsamlDefaultPropertiesService.getDefaultStringParameter(propertyName);
			}

			Logger.debug(DotsamlPropertiesService.class,
					"Found " + propertyName.getPropertyName() + " : " + ((value == null) ? "null" : value));

		} catch (Exception e) {

			Logger.warn(DotsamlPropertiesService.class, "Cast exception on " + propertyName.getPropertyName()
					+ " property. idpConfigId: " + idpConfig.getId());
		}

		return value;
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist
	 * return the passed defaultValue.
	 * 
	 * 
	 * @param idpConfig
	 *            IdpConfig
	 * @param propertyName
	 *            String
	 * @param defaultValue
	 * @return String
	 */
	public static String getOptionString(IdpConfig idpConfig, DotsamlPropertyName propertyName, String defaultValue) {
		String value = null;

		try {
			if (idpConfig.getOptionalProperties().containsKey(propertyName.getPropertyName())) {

				value = (String) idpConfig.getOptionalProperties().get(propertyName.getPropertyName());

			} else {

				value = defaultValue;
			}

			Logger.debug(DotsamlPropertiesService.class,
					"Found " + propertyName.getPropertyName() + " : " + ((value == null) ? "null" : value));

		} catch (Exception e) {

			Logger.warn(DotsamlPropertiesService.class, "Cast exception on " + propertyName.getPropertyName()
					+ " property. idpConfigId: " + idpConfig.getId());
		}

		return value;
	}

	/**
	 * Get String array (comma separated)
	 * 
	 * @param idpConfig
	 *            IdpConfig
	 * @param propertyName
	 *            String
	 * @return String array
	 */
	public static String[] getOptionStringArray(IdpConfig idpConfig, DotsamlPropertyName propertyName) {
		String[] array = null;

		try {
			if (idpConfig.getOptionalProperties().containsKey(propertyName.getPropertyName())) {
				
				array = StringUtils.split(
						(String) idpConfig.getOptionalProperties().get(propertyName.getPropertyName()),
						DotSamlConstants.ARRAY_SEPARATOR_CHAR);
			} else {
				
				String str = DotsamlDefaultPropertiesService.getDefaultStringParameter(propertyName);
				if (str != null) {
					array = StringUtils.split(str, DotSamlConstants.ARRAY_SEPARATOR_CHAR);
				}
			}

			Logger.debug(DotsamlPropertiesService.class,
					"Found " + propertyName.getPropertyName() + " : " + ((array == null) ? "null" : array));

		} catch (Exception e) {

			Logger.warn(DotsamlPropertiesService.class, "Cast exception on " + propertyName.getPropertyName()
					+ " property. idpConfigId: " + idpConfig.getId());
		}
		return array;
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist
	 * returns defaultValue
	 * 
	 * @param idpConfig
	 *            IdpConfig
	 * @param propertyName
	 *            String
	 *
	 * @return Integer
	 */
	public static Integer getOptionInteger(IdpConfig idpConfig, DotsamlPropertyName propertyName) {
		Integer value = null;

		try {
			if (idpConfig.getOptionalProperties().containsKey(propertyName.getPropertyName())) {

				String property = (String) idpConfig.getOptionalProperties().get(propertyName.getPropertyName());
				value = Integer.parseInt(property);

			} else {

				value = DotsamlDefaultPropertiesService.getDefaultIntegerParameter(propertyName);
			}

			Logger.debug(DotsamlPropertiesService.class,
					"Found " + propertyName.getPropertyName() + " : " + ((value == null) ? "null" : value));

		} catch (Exception e) {

			Logger.warn(DotsamlPropertiesService.class, "Cast exception on " + propertyName.getPropertyName()
					+ " property. idpConfigId: " + idpConfig.getId());
		}

		return value;
	}

}
