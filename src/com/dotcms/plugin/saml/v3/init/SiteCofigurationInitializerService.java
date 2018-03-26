package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.SiteConfigurationResolver;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationService;
import com.dotcms.plugin.saml.v3.content.HostService;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotmarketing.beans.Host;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;

import com.liferay.util.InstancePool;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This service is in charge of create/refresh the configuration and set the
 * services needed to get the saml configuration.
 * 
 * @author jsanca
 */
public class SiteCofigurationInitializerService implements Initializer
{
	private static final long serialVersionUID = 1763769161434377458L;
	public static final String HOST_LIST_CONTEXT_KEY = "hostList";
	private final AtomicBoolean isDone = new AtomicBoolean( false );
	private final SiteConfigurationParser siteConfigurationParser = new SiteConfigurationParser();

	@Override
	public void init( final Map<String, Object> context )
	{
		this.isDone.set( false );

		final SiteConfigurationService siteConfigurationService;
		final Map<String, Configuration> configurationMap;
		final Map<String, Configuration> disableConfigurationMap;
		final List<Host> hostList = (List<Host>) context.get( HOST_LIST_CONTEXT_KEY );

		try
		{
			Logger.debug( this, "Parsing SAML configuration" );
			configurationMap = ( null != hostList ) ? this.siteConfigurationParser.getConfiguration( hostList ) : this.siteConfigurationParser.getConfiguration();

			disableConfigurationMap = ( null != hostList ) ? this.siteConfigurationParser.getConfigurationForDisableHosts( hostList ) : this.siteConfigurationParser.getConfigurationForDisableHosts();
		}
		catch ( IOException | DotDataException | DotSecurityException e )
		{
			Logger.error( this, e.getMessage(), e );
			throw new DotSamlException( e.getMessage(), e );
		}

		Logger.debug( this, "SAML configuration, map = " + configurationMap );
		Logger.debug( this, "SAML configuration for disabled saml sites, map = " + disableConfigurationMap );
		if ( null != hostList && null != InstancePool.get( SiteConfigurationService.class.getName() ) )
		{
			this.update( hostList, configurationMap, disableConfigurationMap );
		}
		else
		{
			final SiteConfigurationResolver siteConfigurationResolver = new SiteConfigurationResolver();

			siteConfigurationService = new SiteConfigurationService( configurationMap );
			siteConfigurationService.updateDisableConfiguration( disableConfigurationMap );

			InstancePool.put( SiteConfigurationService.class.getName(), siteConfigurationService );
			InstancePool.put( SiteConfigurationResolver.class.getName(), siteConfigurationResolver );
		}

		this.isDone.set( true );
	}

	private void update( final List<Host> hostList, final Map<String, Configuration> configurationMap, final Map<String, Configuration> disableConfigurationMap )
	{
		Logger.debug( this, "This is a SAML configuration update..." );
		final SiteConfigurationService siteConfigurationService = (SiteConfigurationService) InstancePool.get( SiteConfigurationService.class.getName() );
		final HostService hostService = (HostService) InstancePool.get( HostService.class.getName() );

		siteConfigurationService.updateConfigurations( configurationMap );
		siteConfigurationService.updateDisableConfiguration( disableConfigurationMap );

		// if a host in the list, does not retrieve any configuration, means it is invalid or has been disabled.
		for ( final Host host : hostList )
		{
			if ( !configurationMap.containsKey( host.getHostname() ) )
			{
				Logger.debug( this, "Removing the configuration for: " + host.getHostname() + " and aliases, them have been removed/unpublish/archive/disable" );
				remove( host, hostService, siteConfigurationService );
			}
		}

	}

	private void remove( final Host host, final HostService hostService, final SiteConfigurationService siteConfigurationService )
	{
		if ( null != host )
		{
			siteConfigurationService.setConfigurationBySite( host.getHostname(), null );
			hostService.getHostAlias( host ).forEach( alias -> siteConfigurationService.setConfigurationBySite( alias, null ) );
		}
	}

	@Override
	public boolean isInitializationDone()
	{
		return this.isDone.get();
	}

}
