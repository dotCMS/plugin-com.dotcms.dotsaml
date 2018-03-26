package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.config.SiteConfigurationService;
import com.dotcms.plugin.saml.v3.content.HostService;
import com.dotcms.plugin.saml.v3.content.UpdatedHostResult;
import com.dotcms.repackage.org.apache.commons.lang.time.StopWatch;
import com.dotmarketing.beans.Host;
import com.dotmarketing.util.Logger;

import com.liferay.util.InstancePool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleUpdaterTask
{
	private static final ScheduleUpdaterTask INSTANCE = new ScheduleUpdaterTask();

	public static void reRunScheduleUpdaterTask()
	{
		INSTANCE.updateConfiguration();
	}

	public void updateConfiguration()
	{
		final StopWatch stopWatch = new StopWatch();

		try
		{
			stopWatch.start();
			Logger.debug( this, "ReRunning the Scheduled Updater SAML Task" );

			final SiteCofigurationInitializerService siteCofigurationInitializerService = (SiteCofigurationInitializerService) InstancePool.get( SiteCofigurationInitializerService.class.getName() );
			final SiteConfigurationService siteConfigurationService = (SiteConfigurationService) InstancePool.get( SiteConfigurationService.class.getName() );
			final HostService hostService = (HostService) InstancePool.get( HostService.class.getName() );

			final UpdatedHostResult updatedHostResult = hostService.getUpdatedHosts();
			final List<Host> updatedHosts = updatedHostResult.getUpdatedHosts();
			final List<String> removedHosts = updatedHostResult.getRemovedHosts();

			if ( null != updatedHosts && updatedHosts.size() > 0 )
			{
				Logger.debug( this, "Scheduled Updater SAML Task is updating the modified hosts, number: " + updatedHosts.size() );
				siteCofigurationInitializerService.init( this.wrapInMap( updatedHosts ) );
			}

			if ( null != removedHosts && removedHosts.size() > 0 )
			{
				Logger.debug( this, "Scheduled Updater SAML Task is removing the non-published hosts" );
				removedHosts.forEach( removeHost -> this.removeHostAndAlias( removeHost, hostService, siteConfigurationService ) );
			}

		}
		catch ( Throwable throwable )
		{

			Logger.error( this, throwable.getMessage(), throwable );
		}
		finally
		{
			stopWatch.stop();

			Logger.debug( this, "Scheduled Updater SAML Task is already done, it took: " + stopWatch.getTime() + " ms" );
		}

	}

	private void removeHostAndAlias( final String hostname, final HostService hostService, final SiteConfigurationService siteConfigurationService )
	{
		if ( null != hostname )
		{
			siteConfigurationService.setConfigurationBySite( hostname, null );
			hostService.getHostAlias( hostname ).forEach( alias -> siteConfigurationService.setConfigurationBySite( alias, null ) );
		}
	}

	private Map<String, Object> wrapInMap( final List<Host> updatedHosts )
	{
		final Map<String, Object> context = new HashMap<>();
		context.put( SiteCofigurationInitializerService.HOST_LIST_CONTEXT_KEY, updatedHosts );
		return context;
	}
}
