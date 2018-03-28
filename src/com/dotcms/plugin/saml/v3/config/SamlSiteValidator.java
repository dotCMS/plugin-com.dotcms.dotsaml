package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.SamlUtils;
import com.dotcms.plugin.saml.v3.content.SamlContentTypeUtil;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This class is in charge of validating site configuration based on the biz
 * rules.
 * 
 * @author jsanca
 */

/**
 * Identified as part of Version 3 SAML configuration.  Will most likely be removed.
 *
 * @deprecated 
 */
@Deprecated
public class SamlSiteValidator
{
	private final Set<String> fieldsToValidate = new HashSet<>(); // todo: on 4.x immutable
	{
		this.fieldsToValidate.add( DotSamlConstants.DOTCMS_SAML_IDP_METADATA_PATH );
		this.fieldsToValidate.add( DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH );
		this.fieldsToValidate.add( DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD );
	}

	private final Set<String> fieldsToValidateOnDisabled = new HashSet<>(); // todo: on 4.x immutable
	{
		this.fieldsToValidateOnDisabled.add( DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH );
		this.fieldsToValidateOnDisabled.add( DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD );
	}

	private final Set<String> fileFieldsOnDisabled = new HashSet<>(); // todo: on 4.x immutable
	{
		this.fileFieldsOnDisabled.add( DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH );
	}

	private final Set<String> fileFields = new HashSet<>(); // todo: on 4.x immutable
	{
		this.fileFields.add( DotSamlConstants.DOTCMS_SAML_IDP_METADATA_PATH );
		this.fileFields.add( DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH );
	}

	/**
	 * Validate the saml site configuration. We need to validate these
	 * properties: idp.metadata.path (File) keystore.path (File)
	 * keystore.password keyentryid keystore.entry.password remove.roles.prefix
	 * include.roles.pattern 1. Validate that the properties exist. 2. Validate
	 * that the File properties exist, can access and readIdpConfigs. 3.
	 * Validate we can readIdpConfigs the Key Store. Assumes you send the
	 * defaultHost.
	 *
	 * @param hostName
	 *            String
	 * @param samlConfiguration
	 *            String
	 * @param samlAuthentication
	 *            String
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	public void validateSiteConfiguration( final String hostName, final String samlConfiguration, final String samlAuthentication ) throws DotDataException, DotSecurityException
	{
		final boolean isDisabled = SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DISABLED.equalsIgnoreCase( samlAuthentication );

		if ( samlConfiguration != null )
		{
			final Properties samlProperties = new Properties();

			try
			{
				samlProperties.load( new StringReader( samlConfiguration ) );

				if ( isDisabled )
				{
					if ( hasConfiguration( samlProperties ) )
					{
						Logger.debug( this, "Doing validation for disable hostName: " + hostName );
						doValidationForDisabledSite( samlProperties, hostName );
					}
				}
				else
				{
					doValidationForEnabledSite( samlProperties, hostName );
				}

			}
			catch ( IOException e )
			{
				throw new DotContentletValidationException( "Error trying to parse SAML Field Properties", e );
			}
		}
	}

	/**
	 * Determine if the saml properties has set the minimum configuration, even
	 * for a disabled site.
	 * 
	 * @param samlProperties
	 *            Properties
	 * @return boolean
	 */
	public boolean hasConfiguration( final Properties samlProperties )
	{
		/*
		 * If keystore.path and keystore.password are entered, then we validate
		 * that and any other params needed for sp metadata generation _except_
		 * idp.metadata.path
		 */
		final Set<String> missingFields = SamlUtils.getMissingProperties( samlProperties, fieldsToValidateOnDisabled );

		return missingFields.isEmpty(); // not missing these too, so go ahead and validate it
	}

	/**
	 * Determine if the saml properties has set the minimum configuration, even
	 * for a disabled site.
	 * 
	 * @param samlConfiguration
	 *            String
	 * @return boolean
	 */
	public boolean hasConfiguration( final String samlConfiguration )
	{
		final Properties samlProperties = new Properties();

		try
		{
			samlProperties.load( new StringReader( samlConfiguration ) );
		}
		catch ( IOException e )
		{
			return false;
		}

		return this.hasConfiguration( samlProperties );
	}

	private void doValidationForDisabledSite( final Properties samlProperties, final String hostName )
	{
		Logger.debug( this, "Validation for disabled site: " + hostName );
		this.doValidationForSite( samlProperties, this.fieldsToValidateOnDisabled, this.fileFieldsOnDisabled, hostName );
		Logger.debug( this, "The Validation for disabled site: " + hostName + ", is OK" );
	}

	private void doValidationForEnabledSite( final Properties samlProperties, final String hostName )
	{
		Logger.debug( this, "Standard Validation for site: " + hostName );
		this.doValidationForSite( samlProperties, this.fieldsToValidate, this.fileFields, hostName );

		Logger.debug( this, "The Standard Validation for site: " + hostName + ", is OK" );
	}

	private void doValidationForSite( final Properties samlProperties, final Set<String> siteFieldsToValidate, final Set<String> siteFileFields, final String hostName )
	{
		Logger.debug( this, "Checking the site: " + hostName + ", with the SAMLConfig: " + samlProperties );

		//Validate that these properties exist.
		final Set<String> missingFields = SamlUtils.getMissingProperties( samlProperties, siteFieldsToValidate );

		//Specific Validations for Files.
		final Set<String> missingFiles = SamlUtils.validateFiles( samlProperties, siteFileFields );

		final Set<String> keyStoreErrors = SamlUtils.validateKeyStore( samlProperties );

		final StringBuilder errorHtml = new StringBuilder();
		final StringBuilder errorDebug = new StringBuilder();

		if ( !missingFields.isEmpty() )
		{
			errorHtml.append( "<h3>Missing Fields: </h3>" );
			errorHtml.append( "<ul>" );
			missingFields.forEach( missingField -> errorHtml.append( "<li>" ).append( missingField ).append( "</li>" ) );
			errorHtml.append( "</ul>" );

			errorDebug.append( "\nMissing Fields: \n" );
			errorDebug.append( org.apache.commons.lang.StringUtils.join( missingFields, ',' ) );
		}

		if ( !missingFiles.isEmpty() )
		{
			errorHtml.append( "<h3>Can NOT open Files: </h3>" );
			errorHtml.append( "<ul>" );
			missingFiles.forEach( missingFile -> errorHtml.append( "<li>" ).append( missingFile ).append( "</li>" ) );
			errorHtml.append( "</ul>" );

			errorDebug.append( "\nCan NOT open Files: \n" );
			errorDebug.append( org.apache.commons.lang.StringUtils.join( missingFiles, ',' ) );
		}

		if ( !keyStoreErrors.isEmpty() )
		{
			errorHtml.append( "<h3>Key Store Errors: </h3>" );
			errorHtml.append( "<ul>" );
			keyStoreErrors.forEach( keyStoreError -> errorHtml.append( "<li>" ).append( keyStoreError ).append( "</li>" ) );
			errorHtml.append( "</ul>" );

			errorDebug.append( "\nKey Store Errors: \n" );
			errorDebug.append( org.apache.commons.lang.StringUtils.join( keyStoreErrors, ',' ) );
		}

		// DOTCMS_SAML_BUILD_ROLES
		this.validateBuildRoles( samlProperties, errorHtml, errorDebug );

		Logger.debug( this, "Validation errors: " + errorDebug );

		//If errorHtml has any message, throw the Exception with it.
		if ( UtilMethods.isSet( errorHtml.toString() ) )
		{
			Logger.error( this, "Errors validating SAML Field config: " + errorDebug.toString() );
			throw new DotContentletValidationException( errorHtml.toString() );
		}

	}

	private void validateBuildRoles( final Properties samlProperties, final StringBuilder errorHtml, final StringBuilder errorDebug )
	{
		final String buildRoles = samlProperties.getProperty( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES );
		Logger.debug( this, "Checking build.roles: " + buildRoles );

		if ( UtilMethods.isSet( buildRoles ) )
		{
			if ( !checkBuildRoles( buildRoles ) )
			{
				errorHtml.append( "<h3>Build Roles: </h3>" );
				errorHtml.append( "<i>Invalid value for: " + DotSamlConstants.DOTCMS_SAML_BUILD_ROLES + ", please use a valid one:</i>" );
				errorHtml.append( "<ul>" );
				errorHtml.append( "<li>" ).append( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_ALL_VALUE ).append( "</li>" );
				errorHtml.append( "<li>" ).append( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_IDP_VALUE ).append( "</li>" );
				errorHtml.append( "<li>" ).append( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE ).append( "</li>" );
				errorHtml.append( "<li>" ).append( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_STATIC_ADD_VALUE ).append( "</li>" );
				errorHtml.append( "<li>" ).append( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_NONE_VALUE ).append( "</li>" );
				errorHtml.append( "</ul>" );

				errorDebug.append( "\nBuild Roles Errors: not valid value:" + buildRoles + " \n" );
			}
			else
			{
				// if a valid role
				this.validateStaticOnly( buildRoles, samlProperties, errorHtml, errorDebug );
			}
		}

	}

	private void validateStaticOnly( final String buildRoles, final Properties samlProperties, final StringBuilder errorHtml, final StringBuilder errorDebug )
	{
		final String roleExtra = samlProperties.getProperty( DotSamlConstants.DOTCMS_SAML_OPTIONAL_USER_ROLE );

		if ( DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE.equalsIgnoreCase( buildRoles ) && !UtilMethods.isSet( roleExtra ) )
		{
			errorHtml.append( "<h3>Invalid Static Only Build Role: </h3>" );
			errorHtml.append( "<p>If the Build role is: " + DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE );
			errorHtml.append( ", the " + DotSamlConstants.DOTCMS_SAML_OPTIONAL_USER_ROLE + " must be set" );

			errorDebug.append( "\nBuild Roles Errors: On staticonly, role.extra must be set \n" );
		}

	}

	public static boolean checkBuildRoles( final String buildRolesProperty )
	{
		return DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_ALL_VALUE.equalsIgnoreCase( buildRolesProperty ) || DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_IDP_VALUE.equalsIgnoreCase( buildRolesProperty ) || DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE.equalsIgnoreCase( buildRolesProperty ) || DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_STATIC_ADD_VALUE.equalsIgnoreCase( buildRolesProperty ) || DotSamlConstants.DOTCMS_SAML_BUILD_ROLES_NONE_VALUE.equalsIgnoreCase( buildRolesProperty );
	}
}
