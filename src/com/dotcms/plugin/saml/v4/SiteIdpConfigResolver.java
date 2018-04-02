package com.dotcms.plugin.saml.v4;

import com.dotcms.plugin.saml.v4.config.IdpConfig;
import com.dotcms.plugin.saml.v4.config.IdpConfigHelper;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;

import java.io.IOException;
import java.io.Serializable;

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
	 * @return Configuration
	 */
	public IdpConfig resolveIdpConfig( final HttpServletRequest request ) throws DotDataException, IOException, JSONException
	{
		final String serverName = request.getServerName();
		return this.resolveIdpConfig( serverName );
	}

	/**
	 * Returns the IdpConfig associate to the current site, if it is not
	 * found a match returns the default one site config.
	 * 
	 * @param serverName {@link String}
	 * @return IdpConfig
	 * @throws d 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	public IdpConfig resolveIdpConfig( final String serverName ) throws DotDataException, IOException, JSONException
	{
		final IdpConfig idpConfig = IdpConfigHelper.getInstance().findSiteIdpConfig( serverName );

		Logger.debug( this, "Resolving the configuration: " + idpConfig + ", for the site: " + serverName + ", sites availables:" + IdpConfigHelper.getInstance().getSiteNames() );

		return idpConfig;
	}

	/**
	 * Tries to find the configuration for the disable host.
	 * 
	 * @param request HttpServletRequest
	 * @return Configuration
	 */
	public IdpConfig findConfigurationForDisableHost( final HttpServletRequest request ) throws DotDataException, IOException, JSONException
	{
		final String serverName = request.getServerName();
		final IdpConfig idpConfig = IdpConfigHelper.getInstance().findSiteIdpConfig( serverName );

		Logger.debug( this, "Resolving the configuration: " + idpConfig + ", for the site: " + serverName + ", sites availables:" + IdpConfigHelper.getInstance().getSiteNames() );

		return idpConfig;
	}
}
