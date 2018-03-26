package com.dotcms.plugin.saml.v3.config;

import com.dotmarketing.util.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates the configuration per site.
 * 
 * @author jsanca
 */
public class SiteConfigurationService implements Serializable
{
	private static final long serialVersionUID = 5559633819873359522L;
	private final Map<String, Configuration> configurationBySiteMap;
	private final Map<String, Configuration> disableConfigurationBySiteMap;

	public SiteConfigurationService()
	{
		this( new ConcurrentHashMap<>() );
	}

	public SiteConfigurationService( final Map<String, Configuration> configurationBySiteMap )
	{
		this.configurationBySiteMap = new ConcurrentHashMap<>( configurationBySiteMap );
		this.disableConfigurationBySiteMap = new ConcurrentHashMap<>();
	}

	/**
	 * Updates a set of configurations.
	 * 
	 * @param configurationBySiteMap
	 *            Map
	 */
	public void updateConfigurations( final Map<String, Configuration> configurationBySiteMap )
	{
		this.configurationBySiteMap.putAll( configurationBySiteMap );
	}

	/**
	 * Updates a set of disable configurations. These configuration are only
	 * used as a fallback to figure out the metadata when the configuration is
	 * disable.
	 * 
	 * @param configurationBySiteMap
	 *            Map
	 */
	public void updateDisableConfiguration( final Map<String, Configuration> configurationBySiteMap )
	{
		this.disableConfigurationBySiteMap.putAll( configurationBySiteMap );
	}

	/**
	 * Get the site names
	 * 
	 * @return Set
	 */
	public Set<String> getSiteNames()
	{
		return this.configurationBySiteMap.keySet();
	}

	/**
	 * Get Configuration by site
	 * 
	 * @param site
	 *            {@link String}
	 * @return Configuration
	 */
	public Configuration getConfigurationBySite( final String site )
	{
		Logger.debug( this, ( ( this.configurationBySiteMap.containsKey( site ) ) ? "Found a configuration for the site: " + site : "Could not find a configuration for the site: " + site ) );

		return this.configurationBySiteMap.get( site );
	}

	/**
	 * Get Configuration by disable site This should be use just as a fallback
	 * for things such as the metadata, not for doing authentication or anything
	 * else
	 * 
	 * @param site
	 *            {@link String}
	 * @return Configuration
	 */
	public Configuration getConfigurationByDisabledSite( final String site )
	{
		Logger.debug( this, ( ( this.disableConfigurationBySiteMap.containsKey( site ) ) ? "Found a configuration for the disable site: " + site : "Could not find a configuration for the disable site: " + site ) );

		return this.disableConfigurationBySiteMap.get( site );
	}

	public void setConfigurationBySite( final String site, final Configuration conf )
	{
		this.configurationBySiteMap.remove( site );

		if ( conf != null )
		{
			this.configurationBySiteMap.put( site, conf );
		}

	}
}
