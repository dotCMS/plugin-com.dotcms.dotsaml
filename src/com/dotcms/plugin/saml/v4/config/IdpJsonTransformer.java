package com.dotcms.plugin.saml.v4.config;

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
				jsonObject.put( key, properties.getProperty( key ) );
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

			properties.setProperty( key, value );
		}

		return properties;
	}

	public static JSONObject idpToJson( IdpConfig idpConfig ) throws JSONException, IOException
	{
		JSONObject jsonObject = new JSONObject();

		jsonObject.put( "id", idpConfig.getId() );
		jsonObject.put( "idpName", idpConfig.getIdpName() );
		jsonObject.put( "enabled", idpConfig.isEnabled() );
		jsonObject.put( "sPIssuerURL", idpConfig.getSpIssuerURL() );
		jsonObject.put( "sPEndpointHostname", idpConfig.getSpEndpointHostname() );
		jsonObject.put( "privateKey", getCanonicalPathIfExists( idpConfig.getPrivateKey() ) );
		jsonObject.put( "publicCert", getCanonicalPathIfExists( idpConfig.getPublicCert() ) );
		jsonObject.put( "idPMetadataFile", getCanonicalPathIfExists( idpConfig.getIdPMetadataFile() ) );
		jsonObject.put( "signatureValidationType", idpConfig.getSignatureValidationType() );
		jsonObject.put( "optionalProperties", getJsonObjectFromProperties( idpConfig.getOptionalProperties() ) );
		jsonObject.put( "sites", SiteJsonTransformer.getJsonObjecFromtMap( idpConfig.getSites() ) );

		return jsonObject;
	}

	public static IdpConfig jsonToIdp( JSONObject jsonObject ) throws JSONException
	{
		IdpConfig idpConfig = new IdpConfig();

		idpConfig.setId( jsonObject.getString( "id" ) );
		idpConfig.setIdpName( jsonObject.getString( "idpName" ) );
		idpConfig.setEnabled( jsonObject.getBoolean( "enabled" ) );
		idpConfig.setSpIssuerURL( jsonObject.getString( "sPIssuerURL" ) );
		idpConfig.setSpEndpointHostname( jsonObject.getString( "sPEndpointHostname" ) );
		idpConfig.setPrivateKey( getFileFromCanonicalPath( jsonObject.getString( "privateKey" ) ) );
		idpConfig.setPublicCert( getFileFromCanonicalPath( jsonObject.getString( "publicCert" ) ) );
		idpConfig.setIdPMetadataFile( getFileFromCanonicalPath( jsonObject.getString( "idPMetadataFile" ) ) );
		idpConfig.setSignatureValidationType( jsonObject.getString( "signatureValidationType" ) );
		idpConfig.setOptionalProperties( getPropertiesFromJsonObject( jsonObject.getJSONObject( "optionalProperties" ) ) );
		idpConfig.setSites( SiteJsonTransformer.getMapFromJsonObject( jsonObject.getJSONObject( "sites" ) ) );

		return idpConfig;
	}
}
