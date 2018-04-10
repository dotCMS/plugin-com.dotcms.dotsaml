package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotcms.plugin.saml.v3.meta.MetadataBean;

import java.util.Collection;

import org.opensaml.security.credential.Credential;

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

	/**
	 * The meta descriptor service is created on the configuration, so we take
	 * advance and return the instance from it.
	 * 
	 * @return MetaDescriptorService
	 */
	// Todo: this could be in the InstancePool
	public MetaDescriptorService getMetaDescriptorService();

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

	/**
	 * Returns the path to mapping the metadata.xml info for SP (Service
	 * Provider), in our case the SP is dotCMS.
	 * 
	 * @return String
	 */
	public String getServiceProviderCustomMetadataPath();
}
