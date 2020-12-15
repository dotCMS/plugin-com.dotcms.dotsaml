package com.dotcms.plugin.saml.v3.config;

import com.dotmarketing.util.json.JSONArray;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// todo: not migrated
public class IdpConfigWriterReader
{
	public static final String IDP_CONFIGS = "samlConfigs";
	public static final String DEFAULT_SAML_CONFIG = "defaultSamlConfig";
	public static final String DISABLE_SAML_SITES = "disabledSamlSites";

	public static String readDefaultIdpConfigId( final File idpConfigFile ) throws IOException, JSONException
	{
		String defaultIdpConfigId = "";

		if ( idpConfigFile.exists() )
		{
			String content = new String( Files.readAllBytes( idpConfigFile.toPath() ) );
			JSONObject jsonObject = new JSONObject( content );
			if ( jsonObject.has( DEFAULT_SAML_CONFIG ) )
			{
				defaultIdpConfigId = jsonObject.getString( DEFAULT_SAML_CONFIG );
			}
		}

		return defaultIdpConfigId;
	}

	public static Map<String, String> readDisabledSiteIds( final File idpConfigFile ) throws IOException, JSONException
	{
		Map<String, String> disabledSites = new HashMap<>();

		if ( idpConfigFile.exists() )
		{
			String content = new String( Files.readAllBytes( idpConfigFile.toPath() ) );
			JSONObject jsonObject = new JSONObject( content );

			if ( jsonObject.has( DEFAULT_SAML_CONFIG ) )
			{
				final JSONObject jsonObjectDisabledSites = jsonObject.getJSONObject( DISABLE_SAML_SITES );
				disabledSites = SiteJsonTransformer.getMapFromJsonObject( jsonObjectDisabledSites );
			}
		}

		return disabledSites;
	}

	@SuppressWarnings( "unchecked" )
	public static List<IdpConfig> readIdpConfigs( final File idpConfigFile ) throws IOException, JSONException
	{
		List<IdpConfig> idpConfigList = new ArrayList<>();

		if ( idpConfigFile.exists() )
		{
			String content = new String( Files.readAllBytes( idpConfigFile.toPath() ) );
			JSONObject jsonObjectFile = new JSONObject( content );
			final JSONArray jsonArray = jsonObjectFile.getJSONArray( IDP_CONFIGS );

			for ( int i = 0; i < jsonArray.size(); i++ )
			{
				// jsonObjectId = UUID:{idpConfigs}
				final JSONObject jsonObjectId = jsonArray.getJSONObject( i );

				// I don't like this hack but we need to get the id.
				Iterator<String> keys = jsonObjectId.keys();
				String idpId = keys.next();

				// Now we can get the real JSONObject.
				final JSONObject jsonObject = jsonObjectId.getJSONObject( idpId );
				final IdpConfig idpConfig = IdpJsonTransformer.jsonToIdp( jsonObject );
				idpConfigList.add( idpConfig );
			}
		}

		return idpConfigList;
	}

	public static File write( List<IdpConfig> idpConfigList, String defaultIdpConfigId, Map<String, String> disabledSitesMap, String idpConfigPath ) throws IOException, JSONException
	{
		JSONArray jsonArray = new JSONArray();
		for ( IdpConfig idpConfig : idpConfigList )
		{
			final JSONObject jsonObjectIdp = IdpJsonTransformer.idpToJson( idpConfig );
			final JSONObject jsonObjectOnlyId = new JSONObject().put( idpConfig.getId(), jsonObjectIdp );
			jsonArray.add( jsonObjectOnlyId );
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put( DEFAULT_SAML_CONFIG, defaultIdpConfigId );
		jsonObject.put( IDP_CONFIGS, jsonArray );
		jsonObject.put( DISABLE_SAML_SITES, SiteJsonTransformer.getJsonObjecFromtMap( disabledSitesMap ) );

		File idpConfigFile = new File( idpConfigPath );
		if ( !idpConfigFile.exists() )
		{
			idpConfigFile.getParentFile().mkdirs();
			idpConfigFile.createNewFile();
		}

		try ( FileWriter file = new FileWriter( idpConfigFile ) )
		{
			file.write( jsonObject.toString() );

		}

		return new File( idpConfigPath );
	}

	public static File writeDefaultIdpConfigId( List<IdpConfig> idpConfigList, String defaultIdpConfigId, String idpConfigPath ) throws IOException, JSONException
	{
		return write( idpConfigList, defaultIdpConfigId, readDisabledSiteIds( new File( idpConfigPath ) ), idpConfigPath );
	}

	public static File writeDefaultIdpConfigId( String defaultIdpConfigId, String idpConfigPath ) throws IOException, JSONException
	{
		return write( readIdpConfigs( new File( idpConfigPath ) ), defaultIdpConfigId, readDisabledSiteIds( new File( idpConfigPath ) ), idpConfigPath );
	}

	public static File writeDisabledSIteIds( Map<String, String> disabledSitesMap, String idpConfigPath ) throws IOException, JSONException
	{
		return write( readIdpConfigs( new File( idpConfigPath ) ), readDefaultIdpConfigId( new File( idpConfigPath ) ), disabledSitesMap, idpConfigPath );
	}

	public static File writeIdpConfigs( List<IdpConfig> idpConfigList, String idpConfigPath ) throws IOException, JSONException
	{
		return write( idpConfigList, readDefaultIdpConfigId( new File( idpConfigPath ) ), readDisabledSiteIds( new File( idpConfigPath ) ), idpConfigPath );
	}
}
