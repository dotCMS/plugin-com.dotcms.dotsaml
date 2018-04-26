package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.cms.login.LoginServiceAPI;

import com.dotcms.plugin.saml.v3.config.EndpointHelper;
import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.MetaDataHelper;
import com.dotcms.plugin.saml.v3.config.OptionalPropertiesHelper;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.service.*;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;
import com.dotcms.plugin.saml.v3.util.MetaDataXMLPrinter;
import com.dotcms.plugin.saml.v3.util.SiteIdpConfigResolver;

import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;

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
import javax.servlet.http.HttpSession;

/**
 * Access filter for SAML plugin, it does the autologin and also redirect to the
 * IDP if the user is not logged in. In addition prints out the metadata.xml
 * information for the dotCMS SP.
 * 
 * @author jsanca
 */
public class SamlAccessFilter extends SamlFilter implements Filter
{
	public SamlAccessFilter()
	{
		super( InstanceUtil.newInstance( Config.getStringProperty( DotSamlConstants.DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME, null ), OpenSamlAuthenticationServiceImpl.class ) );
	}

	@VisibleForTesting
	public SamlAccessFilter( final SamlAuthenticationService samlAuthenticationService )
	{
		super( samlAuthenticationService, new MetaDataXMLPrinter(), WebAPILocator.getHostWebAPI(), WebAPILocator.getLanguageWebAPI(), APILocator.getPermissionAPI(), APILocator.getIdentifierAPI(), APILocator.getContentletAPI(), WebAPILocator.getUserWebAPI(), APILocator.getLoginServiceAPI() );
	}

	@VisibleForTesting
	public SamlAccessFilter( final SamlAuthenticationService samlAuthenticationService, final MetaDataXMLPrinter metaDataXMLPrinter, final HostWebAPI hostWebAPI, final LanguageWebAPI languageWebAPI, final PermissionAPI permissionAPI, final IdentifierAPI identifierAPI, final ContentletAPI contentletAPI, final UserWebAPI userWebAPI, final LoginServiceAPI loginService )
	{
		super( samlAuthenticationService, metaDataXMLPrinter, hostWebAPI, languageWebAPI, permissionAPI, identifierAPI, contentletAPI, userWebAPI, loginService );
	}

	@Override
	public void doFilter( final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain ) throws IOException, ServletException
	{
		final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		HttpSession session = httpServletRequest.getSession();
		String redirectAfterLogin = null;
		boolean isLogoutNeed = false;

		if ( super.isByPass( httpServletRequest, session ) )
		{
			Logger.debug( this, "Using SAML by pass" );
			chain.doFilter( httpServletRequest, httpServletResponse );
			return;
		}

		try
		{
			final IdpConfig idpConfig = SiteIdpConfigResolver.getInstance().resolveIdpConfig( httpServletRequest );

			// If idpConfig is null, means this site does not need SAML processing
			if ( idpConfig != null && idpConfig.isEnabled() )
			{
				isLogoutNeed = OptionalPropertiesHelper.getOptionBoolean( idpConfig, DotSamlConstants.DOTCMS_SAML_IS_LOGOUT_NEED, true );

				// First, check if the current request is the SP metadata xml.
				if ( httpServletRequest.getRequestURI().contains( MetaDataHelper.getServiceProviderCustomMetadataPath( idpConfig ) ) )
				{
					// if its, so print it out in the response and return.
					super.printMetaData( httpServletRequest, httpServletResponse, idpConfig );
					return;
				}

				// check if there is any exception filter path, to avoid to canApply all the logic.
				if ( !super.checkAccessFilters( httpServletRequest.getRequestURI(), EndpointHelper.getAccessFilterArray( idpConfig ) ) && super.checkIncludePath( httpServletRequest.getRequestURI(), EndpointHelper.getIncludePathArray( idpConfig ), httpServletRequest ) )
				{
					// if it is an url to canApply the Saml access logic, determine if the autoLogin is possible
					// the autologin will works if the SAMLArt (Saml artifact id) is in the request query string
					// for artifact resolution or SAMLResponse for post resolution.
					final AutoLoginResult autoLoginResult = super.doAutoLogin( httpServletRequest, httpServletResponse, session, idpConfig );

					if ( !autoLoginResult.isAutoLogin() )
					{
						return; // no continue. Usually no continue when there is a sendRedirect or sendError done.
					}

					// we have to assign again the session, since the doAutoLogin might be renewed.
					session = autoLoginResult.getSession();

					// if the auto login couldn't logged the user, then send it to the IdP login page (if it is not already logged in).
					if ( null == session || super.isNotLogged( httpServletRequest, session ) )
					{
						Logger.debug( this, "User is not logged, processing saml request" );
						super.doRequestLoginSecurityLog( httpServletRequest, idpConfig );

						final String originalRequest = httpServletRequest.getRequestURI() + ( ( null != httpServletRequest.getQueryString() ) ? "?" + httpServletRequest.getQueryString() : StringUtils.EMPTY );

						redirectAfterLogin = ( UtilMethods.isSet( httpServletRequest.getParameter( REFERRER_PARAMETER_KEY ) ) ) ? httpServletRequest.getParameter( REFERRER_PARAMETER_KEY ) :
						// this is safe, just to make a redirection when the user get's logged.
						originalRequest;

						Logger.debug( this.getClass(), "Doing Saml Login Redirection when request: " + redirectAfterLogin );

						// if we don't have a redirect yet
						if ( null != session )
						{
							session.setAttribute( WebKeys.REDIRECT_AFTER_LOGIN, redirectAfterLogin );
							session.setAttribute( ORIGINAL_REQUEST, originalRequest );
						}

						try
						{
							// this will redirect the user to the IdP Login Page.
							super.samlAuthenticationService.authentication( httpServletRequest, httpServletResponse );
						}
						catch ( DotSamlException | DotDataException exception )
						{
							Logger.error( this, "Error on authentication: " + exception.getMessage(), exception );
							Logger.debug( this, "Error on authentication, settings 500 response status." );
							httpServletResponse.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
						}

						return;
					}
				}

				// Starting the logout
				// if it is logout
				if ( isLogoutNeed && session != null && super.isLogoutRequest( httpServletRequest.getRequestURI(), EndpointHelper.getLogoutPathArray( idpConfig ) ) )
				{
					if ( super.doLogout( httpServletResponse, httpServletRequest, session, idpConfig ) )
					{
						return;
					}
				}

			}
			else if ( idpConfig != null && !idpConfig.isEnabled() )
			{
				// if the idpConfig for this host is not enabled
				// we check if the url is a default metadata url.
				if ( httpServletRequest.getRequestURI().contains( DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE ) )
				{
					// if its, so print it out in the response and return.
					if ( super.printMetaData( httpServletRequest, httpServletResponse, idpConfig ) )
					{
						Logger.debug( this, "Metadata printed" );
						return;
					}
				}

			}
			else
			{
				Logger.debug( this, "No idpConfig for the site: " + httpServletRequest.getServerName() + ". Not any SAML filtering for this request: " + httpServletRequest.getRequestURI() );
			}

		}
		catch ( JSONException | DotDataException exception )
		{
			Logger.info( this, "Error reading idpConfig for the site: " + httpServletRequest.getServerName());
		}

		chain.doFilter( httpServletRequest, httpServletResponse );

	}

	@Override
	public void destroy()
	{

	}
}
