package com.dotcms.plugin.saml.v4.config;

import java.util.Collection;

import org.opensaml.security.credential.Credential;

import com.dotcms.plugin.saml.v4.meta.MetadataBean;

/**
 * Provides an interface to read xml metadata file of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
public interface MetaDataHelper
{
	/**
	 * Gets the metadata, null if it can not be created.
	 * 
	 * @return MetadataBean
	 */
	public  MetadataBean getMetaData();

	public Collection<Credential> getSigningCredentials();

	/**
	 * Gets the Identity Provider Destination Single Sign on URL
	 * 
	 * @return String
	 */
	public String getIdentityProviderDestinationSSOURL();

	/**
	 * Gets the Identity Provider Destination Single Logout URL
	 * 
	 * @return String
	 */
	public String getIdentityProviderDestinationSLOURL();
}
