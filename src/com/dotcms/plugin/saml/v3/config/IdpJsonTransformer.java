package com.dotcms.plugin.saml.v3.config;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class IdpJsonTransformer
{
	private static String getCanonicalPathIfExists( File file ) throws IOException
	{
		String canonicalPath = "";

		if ( file != null )
		{
			canonicalPath = file.getCanonicalPath();
		}

		return canonicalPath;
	}

	private static File getFileFromCanonicalPath( String canonicalPath )
	{
		File file = null;

		if ( UtilMethods.isSet( canonicalPath ) )
		{
			File fileFromPath = new File( canonicalPath );

			if ( fileFromPath.exists() )
			{
				file = fileFromPath;
			}
			else
			{
				Logger.error( IdpJsonTransformer.class, "File doesn't exists: " + canonicalPath );
			}

		}

		return file;
	}

	private static JSONObject getJsonObjectFromProperties( Properties properties ) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();

		if ( UtilMethods.isSet( properties ) )
		{
			for ( String key : properties.stringPropertyNames() )
			{
				jsonObject.put( key.trim(), properties.getProperty( key.trim() ) );
			}
		}

		return jsonObject;
	}

	private static Properties getPropertiesFromJsonObject( JSONObject jsonObject ) throws JSONException
	{
		Properties properties = new Properties();
		Iterator<?> keys = jsonObject.keys();

		while ( keys.hasNext() )
		{
			String key = (String) keys.next();
			String value = jsonObject.getString( key );

			properties.setProperty( key.trim(), value.trim() );
		}

		return properties;
	}

	public static JSONObject idpToJson( IdpConfig idpConfig ) throws JSONException, IOException
	{
		JSONObject jsonObject = new JSONObject();

		jsonObject.put( "id", idpConfig.getId().trim() );
		jsonObject.put( "idpName", idpConfig.getIdpName().trim() );
		jsonObject.put( "enabled", idpConfig.isEnabled() );
		jsonObject.put( "sPIssuerURL", idpConfig.getSpIssuerURL().trim() );
		jsonObject.put( "sPEndpointHostname", idpConfig.getSpEndpointHostname().trim() );
		jsonObject.put( "privateKey", getCanonicalPathIfExists( idpConfig.getPrivateKey() ).trim() );
		jsonObject.put( "publicCert", getCanonicalPathIfExists( idpConfig.getPublicCert() ).trim() );
		jsonObject.put( "idPMetadataFile", getCanonicalPathIfExists( idpConfig.getIdPMetadataFile() ).trim() );
		jsonObject.put( "signatureValidationType", idpConfig.getSignatureValidationType().trim() );
		jsonObject.put( "optionalProperties", getJsonObjectFromProperties( idpConfig.getOptionalProperties() ) );
		jsonObject.put( "sites", SiteJsonTransformer.getJsonObjecFromtMap( idpConfig.getSites() ) );

		return jsonObject;
	}

	public static IdpConfig jsonToIdp( JSONObject jsonObject ) throws JSONException
	{
		IdpConfig idpConfig = new IdpConfig();

		idpConfig.setId( jsonObject.getString( "id" ).trim() );
		idpConfig.setIdpName( jsonObject.getString( "idpName" ).trim() );
		idpConfig.setEnabled( jsonObject.getBoolean( "enabled" ) );
		idpConfig.setSpIssuerURL( jsonObject.getString( "sPIssuerURL" ).trim() );
		idpConfig.setSpEndpointHostname( jsonObject.getString( "sPEndpointHostname" ).trim() );
		idpConfig.setPrivateKey( getFileFromCanonicalPath( jsonObject.getString( "privateKey" ).trim() ) );
		idpConfig.setPublicCert( getFileFromCanonicalPath( jsonObject.getString( "publicCert" ).trim() ) );
		idpConfig.setIdPMetadataFile( getFileFromCanonicalPath( jsonObject.getString( "idPMetadataFile" ).trim() ) );
		idpConfig.setSignatureValidationType( jsonObject.getString( "signatureValidationType" ).trim() );
		idpConfig.setOptionalProperties( getPropertiesFromJsonObject( jsonObject.getJSONObject( "optionalProperties" ) ) );
		idpConfig.setSites( SiteJsonTransformer.getMapFromJsonObject( jsonObject.getJSONObject( "sites" ) ) );

		return idpConfig;
	}
}
