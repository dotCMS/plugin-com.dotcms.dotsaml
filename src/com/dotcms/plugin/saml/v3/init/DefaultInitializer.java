package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotcms.plugin.saml.v3.content.HostService;
import com.dotcms.plugin.saml.v3.content.SamlContentTypeUtil;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.hooks.SamlHostPostHook;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.Interceptor;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;

import com.liferay.util.InstancePool;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default initializer Responsibilities: - Init the Java Crypto. - Init Saml
 * Services. - Init Plugin Configuration and meta data.
 *
 * @author jsanca
 */
public class DefaultInitializer implements Initializer
{
	private static final long serialVersionUID = -1037117138378277885L;
	private final AtomicBoolean initDone = new AtomicBoolean( false );
	private final SamlContentTypeUtil samlContentTypeUtil;

	public DefaultInitializer()
	{
		this( new SamlContentTypeUtil() );
	}

	@VisibleForTesting
	public DefaultInitializer( final SamlContentTypeUtil samlContentTypeUtil )
	{
		this.samlContentTypeUtil = samlContentTypeUtil;
	}

	@Override
	public void init( final Map<String, Object> context )
	{
		Logger.info( this, "About to create SAML field under Host Content Type" );
		this.createSAMLFields();
		this.addPostHook();

		Logger.info( this, "Init java crypto" );
		this.initJavaCrypto();

		for ( Provider jceProvider : Security.getProviders() )
		{

			Logger.info( this, jceProvider.getInfo() );
		}

		Logger.info( this, "Init Saml Services" );
		this.initService();

		Logger.info( this, "Init Plugin Configuration" );
		this.initConfiguration();

		Logger.info( this, "Init Schedule Update SAML Configuration task" );
		this.initScheduleUpdaterSAMLConfiguration();

		Logger.info( this, "Saml Init DONE" );

		this.initDone.set( true );
	}

	private void addPostHook()
	{
		final SamlHostPostHook postHook = new SamlHostPostHook();
		final Interceptor interceptor = (Interceptor) APILocator.getContentletAPIntercepter();

		Logger.info( this, "Adding Saml Host Hook" );
		interceptor.delPostHookByClassName( postHook.getClass().getName() );

		try
		{
			interceptor.addPostHook( postHook );
		}
		catch ( InstantiationException | IllegalAccessException | ClassNotFoundException e )
		{
			Logger.error( this, "Error adding SamlHostPostHook", e );
		}
	}

	/**
	 * Init the task that readIdpConfigs the hosts and update the saml
	 * configuration if they changed.
	 */
	private void initScheduleUpdaterSAMLConfiguration()
	{
		final int initialDelay = Config.getIntProperty( DotSamlConstants.SCHEDULE_UPDATER_TASK_INITIAL_DELAY, 10 ); // starts on 10 seconds.
		final int delaySeconds = Config.getIntProperty( DotSamlConstants.SCHEDULE_UPDATER_TASK_DELAY_SECONDS, 10 ); // runs every 10 seconds.

		Logger.debug( this, "Starting the Scheduled Updater SAML Task, initialDelay: " + initialDelay + ", delaySeconds = " + delaySeconds );

		// todo: on 4.x replace this by the DotInitScheduler.getScheduledThreadPoolExecutor
		this.getScheduledThreadPoolExecutor().scheduleWithFixedDelay( ScheduleUpdaterTask::reRunScheduleUpdaterTask, initialDelay, delaySeconds, TimeUnit.SECONDS );
	}

	private ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor()
	{
		return new ScheduledThreadPoolExecutor( 10 );
	}

	/**
	 * 1. Get the Host Structure. 2. Create a SAML field if the Structure
	 * doesn't have one. We need a SAML field(textarea) under the Host structure
	 * in order to have a place to configure SAML for each Site.
	 */
	private void createSAMLFields()
	{
		this.samlContentTypeUtil.checkORCreateSAMLField();
	}

	/**
	 * Inits the app configuration. The configuration By default is executed by
	 * {@link DefaultDotCMSConfiguration} however you can override the
	 * implementation by your own implementation by implementing
	 * {@link Configuration} and setting the classpath on the property
	 * {@link DotSamlConstants}.DOT_SAML_CONFIGURATION_CLASS_NAME on the
	 * dotmarketing-config.properties
	 */
	protected void initConfiguration()
	{
		final SiteCofigurationInitializerService siteCofigurationInitializerService = (SiteCofigurationInitializerService) InstancePool.get( SiteCofigurationInitializerService.class.getName() );

		siteCofigurationInitializerService.init( Collections.emptyMap() );
	}

	/**
	 * Inits the OpenSaml service.
	 */
	protected void initService()
	{
		InstancePool.put( HostService.class.getName(), new HostService() );

		InstancePool.put( SiteConfigurationParser.class.getName(), new SiteCofigurationInitializerService() );

		try
		{
			Logger.info( this, "Initializing" );
			InitializationService.initialize();
		}
		catch ( InitializationException e )
		{
			Logger.error( this, e.getMessage(), e );
			throw new DotSamlException( "Initialization failed" );
		}
	}

	/**
	 * Init Java Crypto stuff.
	 */
	protected void initJavaCrypto()
	{
		final JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer();

		try
		{
			javaCryptoValidationInitializer.init();
		}
		catch ( InitializationException e )
		{
			Logger.error( this, e.getMessage(), e );
		}
	}

	@Override
	public boolean isInitializationDone()
	{
		return this.initDone.get();
	}
}
