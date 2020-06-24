package com.dotcms.plugin.saml.v3.util;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.business.web.WebAPILocator;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolve the Site configuration for
 * 
 * @author jsanca
 */
public class SiteIdpConfigResolver implements Serializable
{
	private static class SingletonHolder
	{
		private static final SiteIdpConfigResolver INSTANCE = new SiteIdpConfigResolver();
	}

	private static final long serialVersionUID = -968358357606794446L;

	public static SiteIdpConfigResolver getInstance()
	{
		return SiteIdpConfigResolver.SingletonHolder.INSTANCE;
	}

	/**
	 * Returns the Configuration associate to the current site, if it is not
	 * found a match returns the default one site config.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @return IdpConfig
	 */
	public IdpConfig resolveIdpConfig( final HttpServletRequest request ) throws DotDataException, IOException, JSONException
	{
		return this.resolveIdpConfig(WebAPILocator.getHostWebAPI()
						.getCurrentHostNoThrow(request).getIdentifier());
	}

	/*
	 * Determines is the passed 'id' is in the 'hosts' as either a host or
	 * alias.  If found in either field, the host string is returned.  This 
	 * helps in determining a host name if an alias is passed.
	 */
	private String getHostId(String id) throws DotDataException {

		// Does the ID have value
		if (!UtilMethods.isSet(id)) {
			throw new DotDataException("Site id is required.");
		}

		List<Host> hosts = null;
		try {
			hosts = APILocator.getHostAPI().findAll(APILocator.getUserAPI().getSystemUser(), false);

		} catch (DotSecurityException e) {

			throw new DotDataException("An error occurred when retrieving all dotCMS Sites");
		}
		
		if ( hosts == null || hosts.isEmpty()){
			throw new DotDataException("There are no Sites in this dotCMS instance.");
		}

		String hostId = findHostId(id, hosts);

		if (hostId == null) {
			throw new DotDataException("Site with ID '" + id + "' was not found.");
		}

		return hostId;
	}

	// Searches the host list and alias for each entry.
	private String findHostId(String id, List<Host> hosts) {

		for (Host h : hosts) {
			if (id.equalsIgnoreCase(h.getHostname())) {
				return h.getHostname();
			}
			if (h.getAliases() != null && !h.getAliases().isEmpty()) {

				String[] aliases = h.getAliases().split("[\\s,]+");
				for (String alias : aliases) {
					if (id.equalsIgnoreCase(alias)) {
						return h.getHostname();
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns the IdpConfig associate to the current site, if it is not
	 * found a match returns the default one site config.
	 * 
	 * @param serverName {@link String}
	 * @return IdpConfig
	 *
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	public IdpConfig resolveIdpConfig( final String serverName ) throws DotDataException, IOException, JSONException
	{
		final IdpConfig idpConfig = IdpConfigHelper.getInstance().findSiteIdpConfig( serverName );

		Logger.debug(this, "Resolving the configuration '" + idpConfig.getIdpName() + "' for site '" + serverName + "'" +
				". Available sites [" + IdpConfigHelper.getInstance().getSiteNames() + "]");

		return idpConfig;
	}

	/**
	 * Tries to find the configuration for the disable host.
	 * 
	 * @param request HttpServletRequest
	 * @return IdpConfig
	 */
	public IdpConfig findConfigurationForDisableHost( final HttpServletRequest request ) throws DotDataException, IOException, JSONException
	{
		final String serverName = request.getServerName();
		final IdpConfig idpConfig = IdpConfigHelper.getInstance().findSiteIdpConfig( serverName );

		Logger.debug(this, "Resolving the configuration '" + idpConfig.getIdpName() + "' for site '" + serverName + "'" +
				". Available sites [" + IdpConfigHelper.getInstance().getSiteNames() + "]");

		return idpConfig;
	}

}
