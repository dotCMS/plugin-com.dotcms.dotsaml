package com.dotcms.plugin.saml.v3.content;

import com.dotcms.repackage.org.apache.commons.lang.time.StopWatch;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is kinda stateful service to keep the tracking of the hosts updates.
 * 
 * @author jsanca
 */
public class HostService
{
	private final HostAPI hostAPI = APILocator.getHostAPI();
	private final UserAPI userAPI = APILocator.getUserAPI();
	private final Map<String, Date> hostByModDateMap = new ConcurrentHashMap<>();

	private List<Host> findAllFromDB() throws DotDataException, DotSecurityException
	{
		final StopWatch stopWatch = new StopWatch();
		List<Host> hosts = null;

		try
		{
			stopWatch.start();

			hosts = this.hostAPI.findAllFromDB( this.userAPI.getSystemUser(), false );
		}
		finally
		{
			stopWatch.stop();

			Logger.debug( this, ( ( null != hosts ) ? hosts.size() : 0 ) + " Sites SAML config fetched in: " + stopWatch.getTime() + " ms" );
		}

		hosts = hosts.stream().filter( this::isHostRunning ).collect( Collectors.toList() );

		Logger.debug( this, ( ( null != hosts ) ? hosts.size() : 0 ) + " returned live hosts!" );

		return hosts;
	}

	private boolean isHostRunning( final Host host )
	{
		boolean isRunning = false;

		try
		{
			isRunning = ( null != host && host.isLive() );
		}
		catch ( Exception e )
		{

			isRunning = false;
		}

		return isRunning;
	}

	/**
	 * Get All hosts, not matter if they are the updated or not.
	 *
	 * @return List of Host
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	public List<Host> getAllHosts() throws DotDataException, DotSecurityException
	{
		final List<Host> hosts = this.findAllFromDB();

		if ( null != hosts )
		{
			this.hostByModDateMap.clear();
			hosts.forEach( host -> this.hostByModDateMap.put( host.getHostname(), host.getModDate() ) );
		}

		return hosts;
	}

	/**
	 * Get all updated host from last time they were requests.
	 * 
	 * @return
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	public UpdatedHostResult getUpdatedHosts() throws DotDataException, DotSecurityException
	{
		final List<Host> hosts = this.findAllFromDB();
		final List<Host> updatedHosts = new ArrayList<>(); // todo: make me immutable on 4.x
		final List<String> removedHosts = new ArrayList<>();
		final Map<String, Date> nonUpdatedHostByModDateMap = new HashMap<>( this.hostByModDateMap );

		if ( null != hosts )
		{
			for ( final Host host : hosts )
			{
				// if it is new
				// or the mod update is not the same (means it is updated)
				if ( !this.hostByModDateMap.containsKey( host.getHostname() ) || ( !this.hostByModDateMap.get( host.getHostname() ).equals( host.getModDate() ) ) )
				{
					updatedHosts.add( host );
					this.hostByModDateMap.put( host.getHostname(), host.getModDate() );
				}

				nonUpdatedHostByModDateMap.remove( host.getHostname() );
			}
		}

		Logger.debug( this, "Updated published hosts, number of hosts to update: " + updatedHosts.size() );
		Logger.debug( this, "Non published hosts, number of hosts to remove: " + nonUpdatedHostByModDateMap.size() );

		for ( final String hostId : nonUpdatedHostByModDateMap.keySet() )
		{
			Logger.debug( this, "Removing non published hostId: " + hostId );
			this.hostByModDateMap.remove( hostId );
			removedHosts.add( hostId );
		}

		return new UpdatedHostResult( updatedHosts, removedHosts );
	}

	public Host findDefaultHost( final String fallbackSite ) throws DotDataException, DotSecurityException
	{
		//Verify if a fallback site is configured and get its SAML configuration
		Logger.debug( this, "Finding the default Host, the fallbackSite: " + fallbackSite );
		// if not fallback use the default host
		return ( UtilMethods.isSet( fallbackSite ) ) ? this.findFallbackSite( fallbackSite ) : this.findDefaultHost();
	}

	private Host findFallbackSite( final String fallbackSite ) throws DotDataException, DotSecurityException
	{
		Logger.debug( this, "Finding the fallbackSite: " + fallbackSite );
		final Host host = this.hostAPI.findByName( fallbackSite, this.userAPI.getSystemUser(), false );

		Logger.debug( this, "The fallbackSite host retrieve is: " + host );

		return host;
	}

	private Host findDefaultHost() throws DotDataException, DotSecurityException
	{
		Logger.debug( this, "Finding the default host" );
		final Host host = this.hostAPI.findDefaultHost( this.userAPI.getSystemUser(), false );

		Logger.debug( this, "The default host retrieve is: " + host );

		return host;
	}

	/**
	 * Get Host Alias
	 * 
	 * @param host
	 *            Host
	 * @return List of String alias
	 */
	public List<String> getHostAlias( final Host host )
	{
		return this.hostAPI.parseHostAliases( host );
	}

	/**
	 * Get Host Alias
	 * 
	 * @param hostname
	 *            Host
	 * @return List of String alias
	 */
	public List<String> getHostAlias( final String hostname )
	{
		Host host = null;

		try
		{
			host = this.hostAPI.findByName( hostname, this.userAPI.getSystemUser(), false );
		}
		catch ( Exception e )
		{
			host = null;
		}

		return ( null != host ) ? this.hostAPI.parseHostAliases( host ) : Collections.emptyList();
	}
}
