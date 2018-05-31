package com.dotcms.plugin.saml.v3.config;

import java.net.MalformedURLException;
import java.net.URL;

import com.dotcms.plugin.saml.v3.exception.InvalidIssuerValueException;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotmarketing.util.UtilMethods;

/**
 * Provides a helper for endpoint urls of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
public class EndpointHelper
{
	/**
	 * In case the user wants some specific customer url, otherwise null. This
	 * URL is used on the metadata to fill out the AssertionConsumerService
	 * 
	 * @param idpConfig IdpConfig
	 * @return String
	 */
	public static String getAssertionConsumerEndpoint( IdpConfig idpConfig )
	{
		// spEndpointHostname is a required field during edit.  Has to have value.
		return DotSamlConstants.HTTPS_SCHEMA
				+ spEndpointHostname( idpConfig ) 
				+ DotSamlConstants.ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP 
				+ "/"
				+ idpConfig.getId();
	}

	/**
	 * In case the user wants some specific logout url, otherwise null. This URL
	 * is used on the metadata to fill out the assertion customer service
	 * 
	 * We are assuming that the issuerUrl which is posted by the Idp and 
	 * SingleLogoutEndpoint which is also posted by the Idp will be on the
	 * same domain and port.
	 * 
	 * @param idpConfig IdpConfig
	 * @return String
	 * @throws MalformedURLException 
	 */
	public static String getSingleLogoutEndpoint( IdpConfig idpConfig ) 
	{
		return DotSamlConstants.HTTPS_SCHEMA
				+ spEndpointHostname( idpConfig ) 
				+ DotSamlConstants.LOGOUT_SERVICE_ENDPOINT_DOTSAML3SP 
				+ "/"
				+ idpConfig.getId();
	}

	/**
	 * Get's the access filter array, which are the exceptional cases to avoid
	 * to evaluate on the
	 * {@link com.dotcms.plugin.saml.v3.filter.SamlAccessFilter} For instance if
	 * you include a file that shouldn't need any mapping, you can use it.
	 * 
	 * @param idpConfig IdpConfig
	 * @return String []
	 */
	public static String[] getAccessFilterArray( IdpConfig idpConfig )
	{
		String accessFilterValues = OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOT_SAML_ACCESS_FILTER_VALUES, null );

		return ( UtilMethods.isSet( accessFilterValues ) ) ? accessFilterValues.split( "," ) : null;
	}

	/**
	 * Returns the logout paths
	 * 
	 * @param idpConfig IdpConfig
	 * @return String[]
	 */
	public static String[] getLogoutPathArray( IdpConfig idpConfig )
	{
		String logoutPathValues = OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOT_SAML_LOGOUT_PATH_VALUES, "/c/portal/logout,/dotCMS/logout,/dotsaml/request/logout" );

		return ( UtilMethods.isSet( logoutPathValues ) ) ? logoutPathValues.split( "," ) : null;
	}

	/**
	 * Get's the include urls to be analized by the open saml plugin, usually
	 * the admin They can be a pattern
	 * 
	 * @param idpConfig IdpConfig
	 * @return String []
	 */
	public static String[] getIncludePathArray( IdpConfig idpConfig )
	{
		//String accessFilterValues = OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOT_SAML_INCLUDE_PATH_VALUES, "^" + DotSamlConstants.ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP + "$," + "^/dotCMS/login.*$," + "^/html/portal/login.*$," + "^/c/public/login.*$," + "^/c/portal_public/login.*$," + "^/c/portal/logout.*$," + "^/dotCMS/logout.*$," + "^/application/login/login.*$," + "^/dotAdmin.*$," + "^" + DotSamlConstants.LOGOUT_SERVICE_ENDPOINT_DOTSAML3SP + "$" );
		String accessFilterValues = OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOT_SAML_INCLUDE_PATH_VALUES, "^" + DotSamlConstants.ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP + "$," + "^/dotCMS/login.*$," + "^/html/portal/login.*$," + "^/c/public/login.*$," + "^/c/portal_public/login.*$," + "^/c/portal/logout.*$," + "^/dotCMS/logout.*$," + "^/application/login/login.*$," + "^/dotAdmin.*$" );

		return ( UtilMethods.isSet( accessFilterValues ) ) ? accessFilterValues.split( "," ) : null;
	}
	
	/*
	 * Utility to trim whitespace and remove the dash at the end if it exists.
	 */
	private static String spEndpointHostname( IdpConfig idpConfig ) {
		String spHostName = idpConfig.getSpEndpointHostname().trim();
		 if (spHostName != null && spHostName.length() > 0 && spHostName.charAt(spHostName.length() - 1) == '/') {
			 spHostName = spHostName.substring(0, spHostName.length() - 1);
		    }
		 
		 return spHostName;
	}
}
