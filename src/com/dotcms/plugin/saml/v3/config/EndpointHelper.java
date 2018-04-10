package com.dotcms.plugin.saml.v3.config;

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

	/**
	 * Get's the access filter array, which are the exceptional cases to avoid
	 * to evaluate on the
	 * {@link com.dotcms.plugin.saml.v3.filter.SamlAccessFilter} For instance if
	 * you include a file that shouldn't need any mapping, you can use it.
	 * 
	 * @return String []
	 */
	String[] getAccessFilterArray();

	/**
	 * Returns the logout paths
	 * 
	 * @return String[]
	 */
	String[] getLogoutPathArray();

	/**
	 * Get's the include urls to be analized by the open saml plugin, usually
	 * the admin They can be a pattern
	 * 
	 * @return String []
	 */
	String[] getIncludePathArray();
}
