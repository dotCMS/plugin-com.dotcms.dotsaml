package com.dotcms.plugin.saml.v3.config;

import com.dotcms.repackage.org.apache.commons.lang.BooleanUtils;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotcms.repackage.org.apache.commons.lang.math.NumberUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * This bean encapsulates just the configuration for a site. The configuration
 * for each site must by defined in a field of the Host content type
 *
 * @author jsanca
 */

/**
 * Identified as part of Version 3 SAML configuration.  Will most likely be removed.
 *
 * @deprecated 
 */
@Deprecated
public class SiteConfigurationBean implements Serializable
{
	private static final long serialVersionUID = 2886740290860876502L;
	private static final char ARRAY_SEPARATOR_CHAR = ',';
	private final Map<String, String> siteConfigMap;

	public SiteConfigurationBean( final Map<String, String> siteConfigMap )
	{
		this.siteConfigMap = Collections.unmodifiableMap( siteConfigMap );
	}

	/**
	 * Get a property value from the config, if it does not exists reutrns the
	 * defaultValue
	 * 
	 * @param propertyName
	 * @return String
	 */
	public String getString( final String propertyName )
	{
		return this.getString( propertyName, null );
	}

	/**
	 * Get a property value from the config, if it does not exists returns null
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return String
	 */
	public String getString( final String propertyName, final String defaultValue )
	{
		String value = defaultValue;

		if ( this.siteConfigMap.containsKey( propertyName ) )
		{

			value = this.siteConfigMap.get( propertyName );
		}

		return value;
	}

	@Override
	public String toString()
	{
		return "SiteConfigurationBean{" + "siteConfigMap=" + siteConfigMap + '}';
	}

	/**
	 * Get Boolean, false if it does not exists.
	 * 
	 * @param propertyName
	 *            String
	 * @return boolean
	 */
	public boolean getBoolean( final String propertyName )
	{
		return this.getBoolean( propertyName, false );
	}

	/**
	 * Get Boolean, if does not exists defaultValue.
	 * 
	 * @param propertyName
	 *            String
	 * @return boolean
	 */
	public boolean getBoolean( final String propertyName, final boolean defaultValue )
	{
		boolean value = defaultValue;

		if ( this.siteConfigMap.containsKey( propertyName ) )
		{

			value = BooleanUtils.toBoolean( this.siteConfigMap.get( propertyName ) );
		}

		return value;
	}

	/**
	 * Get String array (comma separated)
	 * 
	 * @param propertyName
	 *            String
	 * @return String array
	 */
	public String[] getStringArray( final String propertyName )
	{
		String[] array = null;

		if ( this.siteConfigMap.containsKey( propertyName ) )
		{

			array = StringUtils.split( this.siteConfigMap.get( propertyName ), ARRAY_SEPARATOR_CHAR );
		}

		return array;
	}

	/**
	 * Get property as an Integer, default value if does not exists.
	 * 
	 * @param propertyName
	 *            String
	 * @param defaultValue
	 *            int
	 * @return int
	 */
	public int getInteger( final String propertyName, final int defaultValue )
	{
		int value = defaultValue;

		if ( this.siteConfigMap.containsKey( propertyName ) )
		{
			value = NumberUtils.toInt( this.siteConfigMap.get( propertyName ), defaultValue );
		}

		return value;
	}
}
