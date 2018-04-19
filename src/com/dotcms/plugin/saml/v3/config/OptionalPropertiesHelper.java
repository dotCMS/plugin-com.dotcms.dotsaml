package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.key.DotSamlConstants;

import com.dotcms.repackage.org.apache.commons.lang.StringUtils;

import com.dotmarketing.util.Logger;

/**
 * Provides a helper to optional properties of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
public class OptionalPropertiesHelper
{
	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns false
	 * 
	 * @param idpConfig IdpConfig
	 * @param propertyName String
	 * @return boolean
	 */
	public static boolean getOptionBoolean( IdpConfig idpConfig, String propertyName )
	{
		return getOptionBoolean( idpConfig, propertyName, false );
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns defaultValue
	 * 
	 * @param idpConfig IdpConfig
	 * @param propertyName String
	 * @return boolean
	 */
	public static boolean getOptionBoolean( IdpConfig idpConfig, String propertyName, boolean defaultValue )
	{
		boolean value = defaultValue;

		if ( idpConfig.getOptionalProperties().containsKey( propertyName ) )
		{
			try
			{
				value = (boolean) idpConfig.getOptionalProperties().get( propertyName );
			}
			catch ( Exception exception )
			{
				Logger.warn( OptionalPropertiesHelper.class, "Cast exception on " + propertyName + " property. idpConfigId: " + idpConfig.getId() );
			}
		}

		return value;
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns defaultValue
	 * 
	 * @param idpConfig IdpConfig
	 * @param propertyName String
	 * @return String
	 */
	public static String getOptionString( IdpConfig idpConfig, String propertyName )
	{
		return getOptionString( idpConfig, propertyName, null );
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns null
	 * 
	 * @param idpConfig IdpConfig
	 * @param propertyName String
	 * @param defaultValue
	 * @return String
	 */
	public static String getOptionString( IdpConfig idpConfig, String propertyName, String defaultValue )
	{
		String value = defaultValue;

		if ( idpConfig.getOptionalProperties().containsKey( propertyName ) )
		{
			try
			{
				value = (String) idpConfig.getOptionalProperties().get( propertyName );
			}
			catch ( Exception exception )
			{
				Logger.warn( OptionalPropertiesHelper.class, "Cast exception on " + propertyName + " property. idpConfigId: " + idpConfig.getId() );
			}
		}

		return value;
	}

	/**
	 * Get String array (comma separated)
	 * 
	 * @param idpConfig IdpConfig
	 * @param propertyName String
	 * @return String array
	 */
	public static String[] getOptionStringArray( IdpConfig idpConfig, String propertyName, String[] defaultArray )
	{
		String[] array = defaultArray;

		if ( idpConfig.getOptionalProperties().containsKey( propertyName ) )
		{
			array = StringUtils.split( (String) idpConfig.getOptionalProperties().get( propertyName ), DotSamlConstants.ARRAY_SEPARATOR_CHAR );
		}

		return array;
	}

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns defaultValue
	 * 
	 * @param idpConfig IdpConfig
	 * @param propertyName String
	 * @param defaultValue int
	 * @return int
	 */
	public static int getOptionInteger( IdpConfig idpConfig, String propertyName, int defaultValue )
	{
		int value = defaultValue;

		if ( idpConfig.getOptionalProperties().containsKey( propertyName ) )
		{
			try
			{
				value = (int) idpConfig.getOptionalProperties().get( propertyName );
			}
			catch ( Exception exception )
			{
				Logger.warn( OptionalPropertiesHelper.class, "Cast exception on " + propertyName + " property. idpConfigId: " + idpConfig.getId() );
			}
		}

		return value;
	}
}
