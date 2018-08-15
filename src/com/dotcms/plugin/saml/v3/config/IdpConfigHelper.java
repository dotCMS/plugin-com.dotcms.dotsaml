package com.dotcms.plugin.saml.v3.config;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dotcms.plugin.saml.v3.cache.SamlCache;
import com.dotcms.plugin.saml.v3.exception.DotSamlByPassException;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.UUIDGenerator;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;

public class IdpConfigHelper extends IdpConfigFileHelper implements Serializable
{
	private static class SingletonHolder
	{
		private static final IdpConfigHelper INSTANCE = new IdpConfigHelper();
	}

	private static final long serialVersionUID = -4344067412079187451L;

	public static IdpConfigHelper getInstance()
	{
		return IdpConfigHelper.SingletonHolder.INSTANCE;
	}

	private SamlCache samlCache = CacheLocator.getSamlCache();

	public IdpConfigHelper()
	{
		
	}

	public void deleteIdpConfig( IdpConfig idpConfig ) throws IOException, JSONException
	{
		List<IdpConfig> idpConfigList = this.getIdpConfigs();
		String defaultIdpConfigId = this.getDefaultIdpConfigId();

		// We need to clean the defaultIdpConfigId if we are deleting the same IDP.
		if ( idpConfig.getId().equals( defaultIdpConfigId ) )
		{
			defaultIdpConfigId = "";

			// Update cache
			try
			{
				samlCache.removeDefaultIdpConfig();
			}
			catch ( Exception exception )
			{
				//Logger.info( this, "Error writing to SamlCache" );
			}
		}

		if ( idpConfigList.contains( idpConfig ) )
		{
			// Delete from list.
			idpConfig = idpConfigList.get( idpConfigList.indexOf( idpConfig ) );
			idpConfigList.remove( idpConfig );
			IdpConfigWriterReader.writeDefaultIdpConfigId( idpConfigList, defaultIdpConfigId, IDP_FILE_PATH );

			// Delete files from file system.
			super.deleteFile( idpConfig.getPrivateKey() );
			super.deleteFile( idpConfig.getPublicCert() );
			super.deleteFile( idpConfig.getIdPMetadataFile() );
		}
		else
		{
			//Logger.warn( this, "IdpConfig with Id: " + idpConfig.getId() + "no longer exists." );
		}

		// Update cache
		try
		{
			samlCache.removeIdpConfig( idpConfig );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}
	}

	public IdpConfig findIdpConfig( String id ) throws IOException, JSONException, DotDataException, DotSamlByPassException
	{
		if ( UtilMethods.isSet( id ) )
		{
			// Try cache
			IdpConfig idpConfig = this.findIdpConfigCache( id );

			if ( idpConfig == null)
			{
				// Try file system
				idpConfig = this.findIdpConfigFileSystem( id );
			}

			return idpConfig;
		}
		else
		{
			throw new DotDataException( "id is required." );
		}
	}

	private IdpConfig findIdpConfigCache( String id ) throws IOException, JSONException, DotDataException
	{
		IdpConfig idpConfig = null;

		try
		{
			idpConfig = samlCache.getIdpConfig( id );

			return idpConfig;
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error reading SamlCache" );
		}

		return idpConfig;
	}

	private IdpConfig findIdpConfigFileSystem( String id ) throws IOException, JSONException, DotDataException, DotSamlByPassException
	{
		IdpConfig idpConfig = null;
		List<IdpConfig> idpConfigList = this.getIdpConfigsFileSystem();

		for ( IdpConfig config : idpConfigList )
		{
			if ( config.getId().equals( id ) )
			{
				idpConfig = config;
			}
		}

		if ( idpConfig == null )
		{
			throw new DotDataException( "Idp with id: " + id + " not found in file." );
		}

		// Update cache
		try
		{
			samlCache.addIdpConfig( idpConfig );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}

		return idpConfig;
	}

	public IdpConfig findSiteIdpConfig( String site ) throws DotDataException, IOException, JSONException, DotSamlByPassException
	{
		if ( UtilMethods.isSet( site ) )
		{
			// Try cache
			IdpConfig idpConfig = this.findSiteIdpConfigCache( site );

			if ( idpConfig == null )
			{
				// Try file system
				idpConfig = this.findSiteIdpConfigFileSystem( site );
			}

			return idpConfig;
		}
		else
		{
			throw new DotDataException( "site is required." );
		}
	}

	private IdpConfig findSiteIdpConfigCache( String site )
	{
		IdpConfig idpConfig = null;

		try
		{
			idpConfig = samlCache.getSiteIdpConfig( site );

			return idpConfig;
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error reading SamlCache" );
		}

		return idpConfig;
	}

	private IdpConfig findSiteIdpConfigFileSystem( String site ) throws DotDataException, IOException, JSONException, DotSamlByPassException
	{
		IdpConfig idpConfig = null;
		List<IdpConfig> idpConfigList = this.getIdpConfigsFileSystem();

		for ( IdpConfig config : idpConfigList )
		{
			Map<String, String> sites = config.getSites();

			if ( sites.values().contains( site ) )
			{
				idpConfig = config;
			}

		}

		if ( idpConfig == null )
		{
			throw new DotDataException( "Idp for site: " + site + " not found in file." );
		}

		// Update cache
		try
		{
			samlCache.addIdpConfig( idpConfig );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}

		return idpConfig;
	}

	public String getDefaultIdpConfigId() throws IOException, JSONException
	{
		// Try cache
		String idpConfigId = this.getDefaultIdpConfigIdCache();

		if ( !UtilMethods.isSet( idpConfigId ) )
		{
			// Try file system
			idpConfigId = this.getDefaultIdpConfigIdFileSystem();
		}

		return idpConfigId;
	}

	private String getDefaultIdpConfigIdCache()
	{
		String idpConfigId = null;

		try
		{
			idpConfigId = samlCache.getDefaultIdpConfigId();
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}

		return idpConfigId;
	}

	private String getDefaultIdpConfigIdFileSystem() throws IOException, JSONException
	{
		String idpConfigId = IdpConfigWriterReader.readDefaultIdpConfigId( new File( IDP_FILE_PATH ) );

		if ( UtilMethods.isSet( idpConfigId ) )
		{
			// Update cache
			try
			{
				IdpConfig idpConfig = this.findIdpConfig( idpConfigId );
				samlCache.addDefaultIdpConfig( idpConfig );
			}
			catch ( Exception exception )
			{
				//Logger.info( this, "Error writing to SamlCache" );
			}
		}

		return idpConfigId;
	}

	public Map<String, String> getDisabledSiteIds() throws IOException, JSONException
	{
		// Try cache
		Map<String, String> disabledSitesMap = this.getDisabledSiteIdsCache();

		// Try file system
		if ( !UtilMethods.isSet( disabledSitesMap ) )
		{
			disabledSitesMap = this.getDisabledSiteIdsFileSystem();
		}

		return disabledSitesMap;
	}

	public Map<String, String> getDisabledSiteIdsCache()
	{
		Map<String, String> disabledSitesMap = null;

		try
		{
			disabledSitesMap = samlCache.getDisabledSitesMap();
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error reading SamlCache" );
		}

		return disabledSitesMap;
	}

	public Map<String, String> getDisabledSiteIdsFileSystem() throws IOException, JSONException
	{
		Map<String, String> disabledSitesMap = IdpConfigWriterReader.readDisabledSiteIds( new File( IDP_FILE_PATH ) );

		if ( UtilMethods.isSet( disabledSitesMap ) )
		{
			// Update cache
			try
			{
				samlCache.addDisabledSitesMap( disabledSitesMap );
			}
			catch ( Exception exception )
			{
				//Logger.info( this, "Error writing to SamlCache" );
			}
		}

		return disabledSitesMap;
	}

	public List<IdpConfig> getIdpConfigs() throws IOException, JSONException, DotSamlByPassException
	{
		// Try cache
		List<IdpConfig> idpConfigs = this.getIdpConfigsCache();

		if ( idpConfigs.size() == 0 )
		{
			// Try file system
			idpConfigs = this.getIdpConfigsFileSystem();
		}

		return idpConfigs;
	}

	private List<IdpConfig> getIdpConfigsCache() throws IOException, JSONException
	{
		List<IdpConfig> idpConfigs = new ArrayList<IdpConfig>();

		try
		{
			idpConfigs = samlCache.getIdpConfigs();
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error reading SamlCache" );
		}

		return idpConfigs;
	}

	private List<IdpConfig> getIdpConfigsFileSystem() throws IOException, JSONException, DotSamlByPassException
	{
		if ( samlCache.hasDiskBeenRead() ) {
			throw new DotSamlByPassException("Disk Has Been Read.");
		}
		
		List<IdpConfig> idpConfigs = IdpConfigWriterReader.readIdpConfigs( new File( IDP_FILE_PATH ) );

		// Update cache
		try
		{
			// Note, this method also flushes the cache.
			samlCache.addIdpConfigs( idpConfigs );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}
		samlCache.setIdpConfigRead(Boolean.TRUE);
		return idpConfigs;
	}

	public String getSiteNames() throws IOException, JSONException
	{
		List<String> sites = this.getIdpSites();

		return String.join( ", ", sites );
	}

	public List<String> getIdpSites() throws IOException, JSONException
	{
		// Try cache
		List<String> sites = this.getIdpSitesCache();

		if ( sites.size() == 0 )
		{
			// Try file system
			sites = this.getIdpSitesFileSystem();
		}

		return sites;
	}

	private List<String> getIdpSitesCache() throws IOException, JSONException
	{
		List<String> sites = new ArrayList<String>();

		try
		{
			sites = samlCache.getSites();
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error reading SamlCache" );
		}

		return sites;
	}

	private List<String> getIdpSitesFileSystem() throws IOException, JSONException
	{
		List<IdpConfig> idpConfigs = IdpConfigWriterReader.readIdpConfigs( new File( IDP_FILE_PATH ) );
		List<String> sites = new ArrayList<String>();

		idpConfigs.forEach( idpConfig -> {

			if ( idpConfig != null )
			{
				Map<String, String> configSiteMap = idpConfig.getSites();
				Collection<String> configSites = configSiteMap.values();
				sites.addAll( configSites );
			}

		});

		// Update cache
		try
		{
			samlCache.addIdpConfigs( idpConfigs );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}

		return sites;
	}

	private IdpConfig renameIdpConfigFiles( IdpConfig idpConfig ) throws IOException
	{
		if ( UtilMethods.isSet( idpConfig.getPrivateKey() ) )
		{
			idpConfig.setPrivateKey( super.writeCertFile( idpConfig.getPrivateKey(), idpConfig.getId() + ".key" ) );
		}
		if ( UtilMethods.isSet( idpConfig.getPublicCert() ) )
		{
			idpConfig.setPublicCert( super.writeCertFile( idpConfig.getPublicCert(), idpConfig.getId() + ".crt" ) );
		}
		if ( UtilMethods.isSet( idpConfig.getIdPMetadataFile() ) )
		{
			idpConfig.setIdPMetadataFile( super.writeMetadataFile( idpConfig.getIdPMetadataFile(), idpConfig.getId() + ".xml" ) );
		}

		return idpConfig;
	}

	public void saveDisabledSiteIds( Map<String, String> disablebSitesMap ) throws IOException, JSONException
	{
		IdpConfigWriterReader.writeDisabledSIteIds( disablebSitesMap, IDP_FILE_PATH );

		// Update cache
		try
		{
			samlCache.addDisabledSitesMap( disablebSitesMap );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}

	}

	public IdpConfig saveIdpConfig( IdpConfig idpConfig ) throws IOException, JSONException
	{
		List<IdpConfig> idpConfigList = this.getIdpConfigs();

		if ( UtilMethods.isSet( idpConfig.getId() ) )
		{
			// Update.
			idpConfigList.remove( idpConfig );
		}
		else
		{
			// Create.
			idpConfig.setId( UUIDGenerator.generateUuid() );
		}

		// Renaming files
		idpConfig = renameIdpConfigFiles( idpConfig );

		idpConfigList.add( idpConfig );
		IdpConfigWriterReader.writeIdpConfigs( idpConfigList, IDP_FILE_PATH );

		// Update cache
		try
		{
			samlCache.addIdpConfig( idpConfig );
		}
		catch ( Exception exception )
		{
			//Logger.info( this, "Error writing to SamlCache" );
		}

		return idpConfig;
	}

	public void setDefaultIdpConfig( IdpConfig idpConfig ) throws IOException, JSONException, DotDataException
	{
		if ( idpConfig != null )
		{
			IdpConfigWriterReader.writeDefaultIdpConfigId( idpConfig.getId(), IDP_FILE_PATH );

			// Update cache
			try
			{
				samlCache.addDefaultIdpConfig( idpConfig );
			}
			catch ( Exception exception )
			{
				//Logger.info( this, "Error writing to SamlCache" );
			}

		}
		else
		{
			throw new DotDataException( "IdpConfig is null." );
		}
	}

	public void setDefaultIdpConfig( String idpConfigId ) throws IOException, JSONException, DotDataException
	{
		final IdpConfig idpConfig = this.findIdpConfig( idpConfigId );

		if ( idpConfig != null )
		{
			this.setDefaultIdpConfig( idpConfig );
		}
		else
		{
			//Logger.error( this, "IdpConfig with Id: " + idpConfigId + "no longer exists." );
			throw new DotDataException( "IdpConfig with Id: " + idpConfigId + "no longer exists." );
		}
	}
}
