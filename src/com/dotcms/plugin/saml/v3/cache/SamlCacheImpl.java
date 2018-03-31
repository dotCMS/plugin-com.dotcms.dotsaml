package com.dotcms.plugin.saml.v3.cache;

import com.dotcms.plugin.saml.v4.config.IdpConfig;
import com.dotcms.plugin.saml.v4.config.IdpConfigWriterReader;
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
	}

	@Override
	public void addDefaultIdpConfig( IdpConfig idpConfig )
	{
		String tag = "addDefaultIdpConfig( IdpConfig ) ";

		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "idpConfig must have an id." );
		}

		this.cache.put( DEFAULT, idpConfig.getId(), DEFAULT_IDP_CONFIG_GROUP );
		this.addIdpConfig( idpConfig );
	}

	@Override
	public void addIdpConfig( IdpConfig idpConfig )
	{
		String tag = "getIdpConfig( String ) ";

		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "idpConfig must have an id and cannot be default." );
		}

		String idpConfigId = idpConfig.getId();

		this.cache.put( idpConfigId, idpConfig, IDP_CONFIG_GROUP );
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

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." );

		try
		{
			idpIndex = (List<String>) this.cache.get( INDEX, IDP_INDEX_GROUP );
		}
		catch ( DotCacheException dotCacheException )
		{
			Logger.error( this, tag + "idpIndex not found in [" + IDP_INDEX_GROUP + "] cache group: [" + INDEX + "]. Creating new entry.", dotCacheException );
		}

		if ( idpIndex == null )
		{
			idpIndex = new ArrayList<String>();
		}

		idpIndex.remove( idpConfigId );
		idpIndex.add( idpConfigId );

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
				Logger.error( this, tag + "Error adding idpConfig.", exception );
			}
		});
	}

	@Override
	@Deprecated
	public void addSiteIdpConfig( String site, IdpConfig idpConfig )
	{
		String tag = "addSiteIdpConfig( String, IdpConfig ) ";

		site = checkNotNull( site, tag + "site is required." );
		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "IdpConfig must have an id." );
		}

		this.cache.put( site, idpConfig.getId(), SITES_TO_IDP_GROUP );
		this.addIdpConfig( idpConfig );
	}

	@Override
	protected void addSiteIdpConfigId( String site, String idpConfigId )
	{
		String tag = "addSiteIdpConfigId( String, String ) ";

		site = checkNotNull( site, tag + "site is required." );
		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." );

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
				this.addSiteIdpConfigId( identifier, idpConfigId );
				this.addSiteIdpConfigId( hostname, idpConfigId );
			});

		}
		catch ( Exception exception )
		{
			Logger.info( this, tag + "" );
		}

	}

	@Override
	public void clearCache()
	{
		for ( String cacheGroup : getGroups() )
		{
			cache.flushGroup( cacheGroup );
		}
	}

	@Override
	public IdpConfig getDefaultIdpConfig()
	{
		String tag = "getIdpConfig( String ) ";
		IdpConfig idpConfig = null;

		try
		{
			String idpConfigId = (String) this.cache.get( DEFAULT, DEFAULT_IDP_CONFIG_GROUP );
			idpConfig = this.getIdpConfig( idpConfigId );
		}
		catch ( DotCacheException dotCacheException )
		{
			Logger.error( this, tag + "SamlCache entry not found in [" + DEFAULT_IDP_CONFIG_GROUP + "] cache group.", dotCacheException );
		}

		return idpConfig;
	}

	@Override
	public IdpConfig getIdpConfig( String idpConfigId )
	{
		String tag = "getIdpConfig( String ) ";
		IdpConfig idpConfig = null;

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." );

		try
		{
			idpConfig = (IdpConfig) this.cache.get( idpConfigId, IDP_CONFIG_GROUP );
		}
		catch ( DotCacheException dotCacheException )
		{
			Logger.error( this, tag + "SamlCache entry not found in [" + IDP_CONFIG_GROUP + "] cache group: " + idpConfigId, dotCacheException );
		}

		return idpConfig;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List<IdpConfig> getIdpConfigs()
	{
		String tag = "getIdpConfig( String ) ";
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

		}
		catch ( DotCacheException dotCacheException )
		{
			Logger.error( this, tag + "SamlCache read error.", dotCacheException );
		}

		return idpConfigs;
	}

	@Override
	public IdpConfig getSiteIdpConfig( String site )
	{
		String tag = "getSiteIdpConfig( String ) ";
		IdpConfig idpConfig = null;

		site = checkNotNull( site, tag + "site is required." );

		try
		{
			String idpConfigId = (String) this.cache.get( site, SITES_TO_IDP_GROUP );
			idpConfig = this.getIdpConfig( idpConfigId );
		}
		catch ( DotCacheException dotCacheException )
		{
			Logger.error( this, tag + "SamlCache entry not found in [" + SITES_TO_IDP_GROUP + "] cache group: " + site, dotCacheException );
		}

		return idpConfig;
	}

	@Override
	public void refresh()
	{
		String tag = "refresh() ";

		this.clearCache();

		try
		{
			// Read file system
			List<IdpConfig> idpConfigs = IdpConfigWriterReader.readIdpConfigs( new File( idpFilePath ) );

			// Update cache
			this.addIdpConfigs( idpConfigs );
		}
		catch ( IOException | JSONException exception )
		{
			Logger.error( this, tag + "Error refreshing cache from file system.", exception );
		}

	}

	@Override
	public void removeDefaultIdpConfig()
	{
		IdpConfig idpConfig = this.getDefaultIdpConfig();

		this.removeIdpConfig( idpConfig.getId() );
		this.cache.remove( DEFAULT, IDP_CONFIG_GROUP );
	}

	@Override
	public void removeIdpConfig( IdpConfig idpConfig )
	{
		String tag = "removeIdpConfig( IdpConfig ) ";

		idpConfig = checkNotNull( idpConfig, tag + "idpConfig is required." );

		if ( Strings.isNullOrEmpty( idpConfig.getId() ) )
		{
			throw new IllegalArgumentException( tag + "idpConfig must have an id." );
		}

		this.removeIdpConfig( idpConfig.getId() );
	}

	@Override
	public void removeIdpConfig( String idpConfigId )
	{
		String tag = "removeIdpConfig( String ) ";

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." );

		this.cache.remove( idpConfigId, IDP_CONFIG_GROUP );
		this.removeIdpConfigIdFromIndex( idpConfigId );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void removeIdpConfigIdFromIndex( String idpConfigId )
	{
		String tag = "removeIdpConfigFromIndex( String ) ";
		List<String> idpIndex = null;

		idpConfigId = checkNotNull( idpConfigId, tag + "idpConfigId is required." );

		try
		{
			idpIndex = (List<String>) this.cache.get( INDEX, IDP_INDEX_GROUP );
		}
		catch ( DotCacheException dotCacheException )
		{
			Logger.error( this, tag + "idpIndex not found in [" + IDP_INDEX_GROUP + "] cache group: [" + INDEX + "]. Creating new entry.", dotCacheException );
		}

		if ( idpIndex == null )
		{
			idpIndex = new ArrayList<String>();
		}

		idpIndex.remove( idpConfigId );

		this.cache.put( INDEX, idpIndex, IDP_INDEX_GROUP );
	}

	@Override
	protected void removeSiteIdpConfigId( String site )
	{
		String tag = "removeSiteIdpConfigId( String ) ";

		site = checkNotNull( site, tag + "site is required." );

		this.cache.remove( site, SITES_TO_IDP_GROUP );
	}
}
