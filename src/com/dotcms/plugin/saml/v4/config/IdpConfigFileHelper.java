package com.dotcms.plugin.saml.v4.config;

import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;

import com.liferay.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class IdpConfigFileHelper implements Serializable
{
	private static final long serialVersionUID = 2810853018482556705L;

	private static final String SAML = "saml";
	protected final static String ASSETS_PATH = Config.getStringProperty( "ASSET_REAL_PATH", FileUtil.getRealPath( Config.getStringProperty( "ASSET_PATH", "/assets" ) ) );
	protected final static String IDP_FILE_PATH = ASSETS_PATH + File.separator + SAML + File.separator + "config.json";
	protected final static String CERTS_PARENT_PATH = ASSETS_PATH + File.separator + SAML + File.separator + "certs" + File.separator;

	protected final static String METADATA_PARENT_PATH = ASSETS_PATH + File.separator + SAML + File.separator + "metadata" + File.separator;

	public IdpConfigFileHelper()
	{
		
	}

	private static class SingletonHolder
	{
		private static final IdpConfigFileHelper INSTANCE = new IdpConfigFileHelper();
	}

	public static IdpConfigFileHelper getInstance()
	{
		return IdpConfigFileHelper.SingletonHolder.INSTANCE;
	}

	public static String getAssetsPath()
	{
		return ASSETS_PATH;
	}

	public static String getCertsParentPath()
	{
		return CERTS_PARENT_PATH;
	}

	public static String getIdpfilePath()
	{
		return IDP_FILE_PATH;
	}

	public static String getMetadataParentPath()
	{
		return METADATA_PARENT_PATH;
	}

	protected void deleteFile( File fileToDelete )
	{
		if ( fileToDelete != null )
		{
			if ( fileToDelete.exists() )
			{
				fileToDelete.delete();
			}
			else
			{
				Logger.warn( this, "File doesn't exist: " + fileToDelete.getName() );
			}
		}
	}

	protected File writeCertFile( File sourceFile, String fileName ) throws IOException
	{
		return this.writeFile( sourceFile, CERTS_PARENT_PATH, fileName );
	}

	protected File writeFile( File sourceFile, String parentPath, String fileName ) throws IOException
	{
		File targetFile = new File( parentPath + fileName );

		if ( !targetFile.exists() )
		{
			targetFile.getParentFile().mkdirs();
			targetFile.createNewFile();
		}

		final Path movedPath = Files.move( sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING );

		return movedPath.toFile();
	}

	protected File writeMetadataFile( File sourceFile, String fileName ) throws IOException
	{
		return this.writeFile( sourceFile, METADATA_PARENT_PATH, fileName );
	}
}
