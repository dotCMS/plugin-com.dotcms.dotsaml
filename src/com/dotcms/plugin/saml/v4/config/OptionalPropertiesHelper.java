package com.dotcms.plugin.saml.v4.config;

/**
 * Provides an interface to optional properties of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
public interface OptionalPropertiesHelper
{
	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns false
	 * 
	 * @param propertyName String
	 * @return boolean
	 */
	public boolean getOptionBoolean( final String propertyName );

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns defaultValue
	 * 
	 * @param propertyName String
	 * @return boolean
	 */
	public boolean getOptionBoolean( final String propertyName, final boolean defaultValue );

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns defaultValue
	 * 
	 * @param propertyName String
	 * @return String
	 */
	public String getOptionString( final String propertyName );

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns null
	 * 
	 * @param propertyName String
	 * @param defaultValue
	 * @return String
	 */
	public String getOptionString( final String propertyName, final String defaultValue );

	/**
	 * Get String array (comma separated)
	 * 
	 * @param propertyName String
	 * @return String array
	 */
	public String[] getOptionStringArray( final String propertyName, String[] defaultArray );

	/**
	 * Get an optional property value from the idpConfig, if it does not exist returns defaultValue
	 * 
	 * @param propertyName String
	 * @param defaultValue int
	 * @return int
	 */
	public int getOptionInteger( final String propertyName, final int defaultValue );
}
