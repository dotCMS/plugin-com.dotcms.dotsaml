package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.cms.login.LoginServiceAPI;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.service.*;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;
import com.dotcms.plugin.saml.v3.util.MetaDataXMLPrinter;

import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.LanguageWebAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.util.*;
import com.dotmarketing.util.json.JSONException;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SamlMetadataFilter extends SamlFilter implements Filter
{
	public SamlMetadataFilter()
	{
		super( InstanceUtil.newInstance( Config.getStringProperty( DotSamlConstants.DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME, null ), OpenSamlAuthenticationServiceImpl.class ) );
	}

	@VisibleForTesting
	public SamlMetadataFilter( final SamlAuthenticationService samlAuthenticationService )
	{
		super( samlAuthenticationService, new MetaDataXMLPrinter(), WebAPILocator.getHostWebAPI(), WebAPILocator.getLanguageWebAPI(), APILocator.getPermissionAPI(), APILocator.getIdentifierAPI(), APILocator.getContentletAPI(), WebAPILocator.getUserWebAPI(), APILocator.getLoginServiceAPI() );
	}

	@VisibleForTesting
	public SamlMetadataFilter( final SamlAuthenticationService samlAuthenticationService, final MetaDataXMLPrinter metaDataXMLPrinter, final HostWebAPI hostWebAPI, final LanguageWebAPI languageWebAPI, final PermissionAPI permissionAPI, final IdentifierAPI identifierAPI, final ContentletAPI contentletAPI, final UserWebAPI userWebAPI, final LoginServiceAPI loginService )
	{
		super( samlAuthenticationService, metaDataXMLPrinter, hostWebAPI, languageWebAPI, permissionAPI, identifierAPI, contentletAPI, userWebAPI, loginService );
	}

	@Override
	public void init( final FilterConfig filterConfig ) throws ServletException
	{
		// Do nothing
	}

	@Override
	public void doFilter( final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain ) throws IOException, ServletException
	{
		final HttpServletResponse httpServletResponse = (HttpServletResponse) servletRequest;
		final HttpServletRequest httpServletRequest = (HttpServletRequest) servletResponse;

		try
		{
			final String originalRequest = httpServletRequest.getRequestURI();
			final String idpConfigId = originalRequest.substring( originalRequest.lastIndexOf( "/" ) );
			final IdpConfig idpConfig = IdpConfigHelper.getInstance().findIdpConfig( idpConfigId );

			// If idpConfig is null, means this site does not need SAML processing
			if ( idpConfig != null )
			{
				Logger.debug( this, "Processing saml metadata request for idpConfig id: " + idpConfigId );
				super.printMetaData( httpServletRequest, httpServletResponse, idpConfig );
				return;
			}
			else
			{
				Logger.debug( this, "No idpConfig for the site: " + httpServletRequest.getServerName() + ". Not any SAML filtering for this request: " + httpServletRequest.getRequestURI() );
			}

		}
		catch ( JSONException | DotDataException exception )
		{
			Logger.error( this, "Error reading idpConfig for the site: " + httpServletRequest.getServerName(), exception );
		}

		chain.doFilter( httpServletRequest, httpServletResponse );

	}

	@Override
	public void destroy()
	{

	}
}
