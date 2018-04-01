package com.dotcms.plugin.saml.v4.config;

/**
 * Provides an interface to endpoint urls of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
public interface EndpointHelper
{
	/**
	 * In case the user wants some specific customer url, otherwise null. This
	 * URL is used on the metadata to fill out the AssertionConsumerService
	 * 
	 * @return String
	 */
	public String getAssertionConsumerEndpoint();

	/**
	 * In case the user wants some specific logout url, otherwise null. This URL
	 * is used on the metadata to fill out the assertion customer service
	 * 
	 * @return String
	 */
	public String getSingleLogoutEndpoint();
}
