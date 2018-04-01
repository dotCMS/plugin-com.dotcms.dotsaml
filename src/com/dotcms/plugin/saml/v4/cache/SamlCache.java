package com.dotcms.plugin.saml.v4.cache;

import com.dotcms.plugin.saml.v4.config.IdpConfig;

import com.dotmarketing.business.Cachable;

import java.util.List;
import java.util.Map;

/**
 * Provides a caching mechanism to improve response times regarding the access
 * to SAML configurations set in the SAML Portlet.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-27-2018
 */
public abstract class SamlCache implements Cachable
{
	protected static final String DEFAULT = "default";
	protected static final String INDEX = "index";
	protected static final String DISABLED_SITES = "disabled-sites";

	protected static final String DEFAULT_IDP_CONFIG_GROUP = "Default-Ipd-Config";
	protected static final String IDP_CONFIG_GROUP = "Ipd-Config";
	protected static final String SITES_TO_IDP_GROUP = "Sites-To-Idp";
	protected static final String DISABLED_SITES_GROUP = "Disabled-Sites";
	protected static final String IDP_INDEX_GROUP = "Idp-Index";
	protected static final String DISABLED_SITES_INDEX_GROUP = "Disabled-Sites-Index";

	/**
	 * Adds a {@link IdpConfig} to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the config already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param idpConfig - The {@link IdpConfig} object to cache.
	 */
	public abstract void addDefaultIdpConfig( IdpConfig idpConfig );

	/**
	 * Adds a site to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the site already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param site - The disabled site id to cache.
	 */
	protected abstract void addDisabledSiteId( String site );

	/**
	 * Adds sites to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the site already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param sites - The disabled site ids to cache.
	 */
	public abstract void addDisabledSitesMap( Map<String, String> sites );

	/**
	 * Adds a {@link IdpConfig} to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the config already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param rule - The {@link IdpConfig} object to cache.
	 */
	public abstract void addIdpConfig( IdpConfig idpConfig );

	/**
	 * Adds the idpConfigId to the index.
	 * 
	 * @param idpConfigId - The ID of the {@link IdpConfig} object to add to index in cache.
	 */
	protected abstract void addIdpConfigIdToIndex( String idpConfigId );

	/**
	 * Adds a list of {@link IdpConfig} to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the config already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param rule - The {@link IdpConfig} object to cache.
	 */
	public abstract void addIdpConfigs( List<IdpConfig> idpConfigs );

	/**
	 * Adds a site to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the site already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param site - The associated site to cache.
	 * @param idpConfig - The associated {@link IdpConfig} object to cache.
	 */
	public abstract void addSiteIdpConfig( String site, IdpConfig idpConfig );

	/**
	 * Adds a site to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the site already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param site - The associated site to cache.
	 * @param idpConfigId - The ID of the {@link IdpConfig} object to cache.
	 */
	protected abstract void addSiteIdpConfigId( String site, String idpConfigId );

	/**
	 * Adds sites to the caching structure. Null objects or with empty
	 * values will not be added to the cache. In case the site already exists,
	 * the entry will be updated with the new value.
	 * 
	 * @param sites - The associated sites to cache.
	 * @param idpConfigId - The ID of the {@link IdpConfig} object to cache.
	 */
	protected abstract void addSitesIdpConfigId( Map<String, String> sites, String idpConfigId );

	/**
	 * Returns the default {@link IdpConfig} object associated to the DEFAULT key.
	 */
	public abstract IdpConfig getDefaultIdpConfig();

	/**
	 * Returns the default {@link IdpConfig} object id associated to the DEFAULT key.
	 */
	public abstract String getDefaultIdpConfigId();

	/**
	 * Returns all disabled sites.
	 * 
	 * @return All the disabled sites.
	 */
	public abstract Map<String, String> getDisabledSitesMap();

	@Override
	public String[] getGroups()
	{
		return new String[] { DEFAULT_IDP_CONFIG_GROUP, IDP_CONFIG_GROUP, SITES_TO_IDP_GROUP, DISABLED_SITES_GROUP, IDP_INDEX_GROUP, DISABLED_SITES_INDEX_GROUP };
	}

	/**
	 * Returns the {@link IdpConfig} object associated to the specified key.
	 * 
	 * @param idpConfigId - The ID of the {@link IdpConfig} object.
	 * @return The associated {@link IdpConfig} object.
	 */
	public abstract IdpConfig getIdpConfig( String idpConfigId );

	/**
	 * Returns all the {@link IdpConfig} objects.
	 * 
	 * @return All the {@link IdpConfig} objects.
	 */
	public abstract List<IdpConfig> getIdpConfigs();

	@Override
	public String getPrimaryGroup()
	{
		return IDP_CONFIG_GROUP;
	}

	/**
	 * Returns the {@link IdpConfig} object associated to the specified key.
	 * 
	 * @param site - The site associated to the {@link IdpConfig} object.
	 * @return The associated {@link IdpConfig} object.
	 */
	public abstract IdpConfig getSiteIdpConfig( String site );

	/**
	 * Clears and rebuilds the cache
	 */
	public abstract void refresh();

	/**
	 * Removes the default {@link IdpConfig} object from the caching structure.
	 */
	public abstract void removeDefaultIdpConfig();

	/**
	 * Removes the {@link IdpConfig} object from the caching structure.
	 * 
	 * @param idpConfig - The {@link IdpConfig} object that will be removed.
	 */
	public abstract void removeIdpConfig( IdpConfig idpConfig );

	/**
	 * Removes the {@link IdpConfig} object from the caching structure.
	 * 
	 * @param idpConfigId - The ID of the {@link IdpConfig} object that will be removed.
	 */
	protected abstract void removeIdpConfig( String idpConfigId );

	/**
	 * Removes the idpConfigId from the index.
	 * 
	 * @param idpConfigId - The ID of the {@link IdpConfig} object to remove from index in cache.
	 */
	protected abstract void removeIdpConfigIdFromIndex( String idpConfigId );

	/**
	 * Removes the site object from the caching structure.
	 * 
	 * @param site - The site that will be removed.
	 */
	protected abstract void removeSiteIdpConfigId( String site );

	/**
	 * Removes sites from the caching structure.
	 * 
	 * @param sites - The associated sites to remove from the caching structure.
	 * @param idpConfigId - The ID of the {@link IdpConfig} object to cache.
	 */
	protected abstract void removeSitesIdpConfigId( Map<String, String> sites, String idpConfigId );
}
