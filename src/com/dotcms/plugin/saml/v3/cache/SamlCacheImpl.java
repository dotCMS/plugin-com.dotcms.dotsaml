package com.dotcms.plugin.saml.v3.cache;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigWriterReader;

import com.dotcms.repackage.com.google.common.base.Strings;

import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotCacheAdministrator;
import com.dotmarketing.business.DotCacheException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;

import com.liferay.util.FileUtil;

import static com.dotcms.repackage.com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the SAML config caching functionality.
 *
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-27-2018
 */
public class SamlCacheImpl extends SamlCache
{
	protected DotCacheAdministrator cache = null;

	private final String assetsPath;
	private final String idpFilePath;

	/**
	 * Default constructor. Instantiates the {@link DotCacheAdministrator}
	 * object used to store all the configuration information.
	 */
	public SamlCacheImpl()
	{
		cache = CacheLocator.getCacheAdministrator();

		this.assetsPath = Config.getStringProperty( "ASSET_REAL_PATH", FileUtil.getRealPath( Config.getStringProperty( "ASSET_PATH", "/assets" ) ) );
		this.idpFilePath = assetsPath + File.separator + "saml" + File.separator + "config.json";

		//Logger.info( this, "this.assetsPath = " + this.assetsPath );
		//Logger.info( this, "this.idpFilePath = " + this.idpFilePath );
	}

	@Override
	public void addDefaultIdpConfig( IdpConfig idpConfig ) throws DotCacheException
	{
		String tag = "addDefaultIdpConfig( IdpConfig ) ";

		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "idpConfig must have an id." );
		}

		//Logger.info( this, "Adding default idpConfig to cache id = " + idpConfig.getId() );

		this.cache.put( DEFAULT, idpConfig.getId(), DEFAULT_IDP_CONFIG_GROUP );
		this.addIdpConfig( idpConfig );
	}

	@Override
	protected void addDisabledSiteId( String site )
	{
		String tag = "addDisabledSiteId( String ) ";

		site = checkNotNull( site.trim(), tag + "site is required." ).trim();

		//Logger.info( this, "Adding disabled site to cache = " + site.trim() );

		this.cache.put( site, site, DISABLED_SITES_INDEX_GROUP );
	}

	@Override
	public void addDisabledSitesMap( Map<String, String> sites )
	{
		String tag = "addDisabledSitesMap( Map<String, String> ) ";

		try
		{
			sites = checkNotNull( sites, tag + "sites is required." );

			//Logger.info( this, "Flushing DISABLED_SITES_GROUP cache." );
			//Logger.info( this, "Flushing DISABLED_SITES_INDEX_GROUP cache." );

			cache.flushGroup( DISABLED_SITES_GROUP );
			cache.flushGroup( DISABLED_SITES_INDEX_GROUP );

			this.cache.put( DISABLED_SITES, sites, DISABLED_SITES_GROUP );

			sites.forEach( ( identifier, hostname )->{
				this.addDisabledSiteId( identifier.trim() );
				this.addDisabledSiteId( hostname.trim() );
			});

		}
		catch ( Exception exception )
		{
			Logger.error( this, tag + "Error adding disabled sites to cache.", exception );
		}

	}

	@Override
	public void addIdpConfig( IdpConfig idpConfig ) throws DotCacheException
	{
		String tag = "addIdpConfig( IdpConfig ) ";

		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "idpConfig must have an id and cannot be default." );
		}

		String idpConfigId = idpConfig.getId();

		//Logger.info( this, "Adding idpConfig to cache id = " + idpConfig.getId() );

		this.cache.put( idpConfigId, idpConfig, IDP_CONFIG_GROUP );
		incrementIdpCount();
		this.addIdpConfigIdToIndex( idpConfigId );

		Map<String, String> sites = idpConfig.getSites();

		if ( sites != null && sites.size() > 0 )
		{
			this.addSitesIdpConfigId( sites, idpConfigId );
		}

	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void addIdpConfigIdToIndex( String idpConfigId )
	{
		String tag = "addIdpConfigToIndex( String ) ";
		List<String> idpIndex = null;

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." ).trim();

		try
		{
			idpIndex = (List<String>) this.cache.get( INDEX, IDP_INDEX_GROUP );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "idpIndex not found in [" + IDP_INDEX_GROUP + "] cache group: [" + INDEX + "]. Creating new entry." );
		}

		if ( idpIndex == null )
		{
			idpIndex = new ArrayList<String>();
		}

		idpIndex.remove( idpConfigId );
		idpIndex.add( idpConfigId );

		//Logger.info( this, "Adding idpConfig id to index cache id = " + idpConfigId );

		this.cache.put( INDEX, idpIndex, IDP_INDEX_GROUP );
	}

	@Override
	public void addIdpConfigs( List<IdpConfig> idpConfigs )
	{
		String tag = "addIdpConfigs( List<IdpConfig> ) ";


		idpConfigs.forEach( idpConfig ->{
			try
			{
				this.addIdpConfig( idpConfig );

				Map<String, String> sites = idpConfig.getSites();

				if ( sites != null && sites.size() > 0 )
				{
					this.addSitesIdpConfigId( sites, idpConfig.getId() );
				}

			}
			catch ( Exception exception )
			{
				Logger.error( this, tag + "Error adding idpConfig to cache.", exception );
			}
		});
	}

	@Override
	public void addSiteIdpConfig( String site, IdpConfig idpConfig ) throws DotCacheException
	{
		String tag = "addSiteIdpConfig( String, IdpConfig ) ";

		site = checkNotNull( site, tag + "site is required." ).trim();
		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "IdpConfig must have an id." );
		}

		//Logger.info( this, "Adding site to idpConfig id cache site = " + site + " idpConfig id = " + idpConfig.getId() );

		this.cache.put( site, idpConfig.getId(), SITES_TO_IDP_GROUP );
		this.addIdpConfig( idpConfig );
	}

	@Override
	protected void addSiteIdpConfigId( String site, String idpConfigId )
	{
		String tag = "addSiteIdpConfigId( String, String ) ";

		site = checkNotNull( site, tag + "site is required." ).trim();
		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." ).trim();

		//Logger.info( this, "Adding site to idpConfig id cache site = " + site + " idpConfig id = " + idpConfigId );

		this.cache.put( site, idpConfigId, SITES_TO_IDP_GROUP );
	}

	@Override
	protected void addSitesIdpConfigId( Map<String, String> sites, String idpConfigId )
	{
		String tag = "addSitesIdpConfigId( Map<String, String>, String ) ";

		// It's ok for sites to be null,
		// but we need to check before processing
		try
		{
			sites = checkNotNull( sites, tag + "sites is required." );

			sites.forEach( ( identifier, hostname )->{
				this.addSiteIdpConfigId( identifier.trim(), idpConfigId.trim() );
				this.addSiteIdpConfigId( hostname.trim(), idpConfigId.trim() );
			});

		}
		catch ( Exception exception )
		{
			Logger.error( this, tag + "Error adding sites to cache.", exception );
		}

	}

	@Override
	public void clearCache()
	{
		//Logger.info( this, "Flushing Saml cache." );

		for ( String cacheGroup : getGroups() )
		{
			cache.flushGroup( cacheGroup );
		}
	}

	@Override
	public IdpConfig getDefaultIdpConfig()
	{
		String tag = "getDefaultIdpConfig() ";
		IdpConfig idpConfig = null;

		try
		{
			String idpConfigId = (String) this.cache.get( DEFAULT, DEFAULT_IDP_CONFIG_GROUP );
			idpConfig = this.getIdpConfig( idpConfigId );

			//Logger.info( this, "Getting default idpConfig from cache id = " + idpConfigId );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache entry not found in [" + DEFAULT_IDP_CONFIG_GROUP + "] cache group." );
		}

		return idpConfig;
	}

	@Override
	public String getDefaultIdpConfigId()
	{
		String tag = "getDefaultIdpConfigId() ";
		String idpConfigId = null;

		try
		{
			idpConfigId = (String) this.cache.get( DEFAULT, DEFAULT_IDP_CONFIG_GROUP );

			//Logger.info( this, "Getting default idpConfig from cache id = " + idpConfigId );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache entry not found in [" + DEFAULT_IDP_CONFIG_GROUP + "] cache group." );
		}

		return idpConfigId;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Map<String, String> getDisabledSitesMap()
	{
		String tag = "getDisabledSites() ";
		Map<String, String> disabledSitesMap = new HashMap<String, String>();

		try
		{
			disabledSitesMap = (Map<String, String>) this.cache.get( DISABLED_SITES, DISABLED_SITES_INDEX_GROUP );

			//Logger.info( this, "Getting disabled sites map from cache disabledSitesMap = " + disabledSitesMap );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache entry not found in [" + DISABLED_SITES_INDEX_GROUP + "] cache group." );
		}

		return disabledSitesMap;
	}

	@Override
	public IdpConfig getIdpConfig( String idpConfigId )
	{
		String tag = "getIdpConfig( String ) ";
		IdpConfig idpConfig = null;

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." ).trim();

		try
		{
			idpConfig = (IdpConfig) this.cache.get( idpConfigId, IDP_CONFIG_GROUP );

			//Logger.info( this, "Getting idpConfig from cache id = " + idpConfigId );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache entry not found in [" + IDP_CONFIG_GROUP + "] cache group: " + idpConfigId );
		}

		return idpConfig;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List<IdpConfig> getIdpConfigs()
	{
		String tag = "getIdpConfigs() ";
		List<IdpConfig> idpConfigs = new ArrayList<IdpConfig>();

		try
		{
			List<String> idpConfigIds = (List<String>) this.cache.get( INDEX, IDP_INDEX_GROUP );

			if ( idpConfigIds != null && !idpConfigIds.isEmpty() )
			{
				idpConfigIds.forEach( idpConfigId -> {

					IdpConfig idpConfig = this.getIdpConfig( idpConfigId );

					if ( idpConfig != null )
					{
						idpConfigs.add( idpConfig );
					}

				});
			}

			//Logger.info( this, "Getting idpConfigs from cache idpConfigIds.size() = " + idpConfigIds.size() );
			//Logger.info( this, "Getting idpConfigs from cache idpConfigs.size() = " + idpConfigs.size() );

			Integer idpCount = getIdpCount();

			//Logger.info( this, "Checking if idpCount == idpConfigs.size()." );
			//Logger.info( this, idpCount + " == " + idpConfigs.size() + " ?" );

			if ( idpCount != idpConfigs.size() )
			{
				// Return an empty list.
				// This tells the IdpConfigHelper class to
				// try the file system and invalidate the cache.
				return new ArrayList<IdpConfig>();
			}
			else
			{
				//Logger.info( this, "Counts match." );
			}
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache read error." );
		}

		return idpConfigs;
	}

	@Override
	public IdpConfig getSiteIdpConfig( String site )
	{
		String tag = "getSiteIdpConfig( String ) ";
		IdpConfig idpConfig = null;

		site = checkNotNull( site, tag + "site is required." ).trim();

		try
		{
			String idpConfigId = (String) this.cache.get( site, SITES_TO_IDP_GROUP );
			idpConfig = this.getIdpConfig( idpConfigId );

			//Logger.info( this, "Getting site idpConfig from cache site = " + site + " idpConfig id = " + idpConfigId );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache entry not found in [" + SITES_TO_IDP_GROUP + "] cache group: " + site );
		}

		return idpConfig;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List<String> getSites()
	{
		String tag = "getSites() ";
		List<String> sites = new ArrayList<String>();

		try
		{
			List<String> idpConfigIds = (List<String>) this.cache.get( INDEX, IDP_INDEX_GROUP );

			if ( idpConfigIds != null && !idpConfigIds.isEmpty() )
			{
				idpConfigIds.forEach( idpConfigId -> {

					IdpConfig idpConfig = this.getIdpConfig( idpConfigId );

					if ( idpConfig != null )
					{
						Map<String, String> configSiteMap = idpConfig.getSites();
						Collection<String> configSites = configSiteMap.values();
						sites.addAll( configSites );
					}

				});
			}

			//Logger.info( this, "Getting all sites from all idpConfigs from cache idpConfigIds.size() = " + idpConfigIds.size() + " sites.size() = " + sites.size() );

		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "SamlCache read error." );
		}

		return sites;
	}

	private void incrementIdpCount() throws DotCacheException
	{
		Integer count = getIdpCount();
		count++;
		this.cache.put( COUNT, "" + count, IDP_CONFIG_COUNT_GROUP );
	}

	private void decrementIdpCount() throws DotCacheException
	{
		Integer count = getIdpCount();
		count--;
		this.cache.put( COUNT, "" + count, IDP_CONFIG_COUNT_GROUP );
	}

	private Integer getIdpCount() throws DotCacheException
	{
		Integer count =  0;

		try
		{
			count =  Integer.parseInt( (String) this.cache.get( COUNT, IDP_CONFIG_COUNT_GROUP ) );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "IdpConfig count not set in cache. Setting to 0." );

			cache.flushGroup( IDP_CONFIG_COUNT_GROUP );
			this.cache.put( COUNT, "" + 0, IDP_CONFIG_COUNT_GROUP );
		}

		return ( count != null ? count : 0 );
	}



	@Override
	public void removeDefaultIdpConfig() throws DotCacheException
	{
		IdpConfig idpConfig = this.getDefaultIdpConfig();

		Map<String, String> sites = idpConfig.getSites();

		if ( sites != null && sites.size() > 0 )
		{
			this.removeSitesIdpConfigId( sites, idpConfig.getId() );
		}

		this.removeIdpConfig( idpConfig.getId() );
		this.cache.remove( DEFAULT, IDP_CONFIG_GROUP );

		//Logger.info( this, "Removing default idpConfig from cache id = " + idpConfig.getId() );
	}

	@Override
	public void removeIdpConfig( IdpConfig idpConfig ) throws DotCacheException
	{
		String tag = "removeIdpConfig( IdpConfig ) ";

		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "idpConfig must have an id." );
		}

		Map<String, String> sites = idpConfig.getSites();

		if ( sites != null && sites.size() > 0 )
		{
			this.removeSitesIdpConfigId( sites, idpConfig.getId() );
		}

		this.removeIdpConfig( idpConfig.getId() );
	}

	@Override
	protected void removeIdpConfig( String idpConfigId ) throws DotCacheException
	{
		String tag = "removeIdpConfig( String ) ";

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." ).trim();

		this.cache.remove( idpConfigId, IDP_CONFIG_GROUP );
		decrementIdpCount();
		this.removeIdpConfigIdFromIndex( idpConfigId );

		//Logger.info( this, "Removing idpConfig from cache id = " + idpConfigId );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void removeIdpConfigIdFromIndex( String idpConfigId )
	{
		String tag = "removeIdpConfigFromIndex( String ) ";
		List<String> idpIndex = null;

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." ).trim();

		try
		{
			idpIndex = (List<String>) this.cache.get( INDEX, IDP_INDEX_GROUP );
		}
		catch ( DotCacheException dotCacheException )
		{
			//Logger.info( this, tag + "idpIndex not found in [" + IDP_INDEX_GROUP + "] cache group: [" + INDEX + "]. Creating new entry." );
		}

		if ( idpIndex == null )
		{
			idpIndex = new ArrayList<String>();
		}

		idpIndex.remove( idpConfigId );

		this.cache.put( INDEX, idpIndex, IDP_INDEX_GROUP );

		//Logger.info( this, "Removing idpConfig from cache index id = " + idpConfigId );
	}

	@Override
	protected void removeSiteIdpConfigId( String site )
	{
		String tag = "removeSiteIdpConfigId( String ) ";

		site = checkNotNull( site, tag + "site is required." ).trim();

		this.cache.remove( site, SITES_TO_IDP_GROUP );

		//Logger.info( this, "Removing site from cache site = " + site );
	}

	@Override
	protected void removeSitesIdpConfigId( Map<String, String> sites, String idpConfigId )
	{
		String tag = "removeSitesIdpConfigId( Map<String, String>, String ) ";

		// It's ok for sites to be null,
		// but we need to check before processing
		try
		{
			sites = checkNotNull( sites, tag + "sites is required." );

			sites.forEach( ( identifier, hostname )->{
				this.removeSiteIdpConfigId( identifier.trim() );
				this.removeSiteIdpConfigId( hostname.trim() );
			});

		}
		catch ( Exception exception )
		{
			Logger.info( this, tag + "Error removing sites from cache." );
		}

	}
}
