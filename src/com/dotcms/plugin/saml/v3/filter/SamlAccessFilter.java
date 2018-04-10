package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.cms.login.LoginServiceAPI;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.exception.NotNullEmailAllowedException;
import com.dotcms.plugin.saml.v3.exception.SamlUnauthorizedException;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.service.*;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;
import com.dotcms.plugin.saml.v3.util.MetaDataXMLPrinter;
import com.dotcms.plugin.saml.v3.util.SamlUtils;
import com.dotcms.plugin.saml.v3.util.SiteIdpConfigResolver;

import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.LanguageWebAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.util.*;
import com.dotmarketing.util.json.JSONException;

import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.auth.PrincipalThreadLocal;
import com.liferay.portal.model.User;
import com.liferay.portal.servlet.PortletSessionPool;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * Access filter for SAML plugin, it does the autologin and also redirect to the
 * IDP if the user is not logged in. In addition prints out the metadata.xml
 * information for the dotCMS SP.
 * 
 * @author jsanca
 */
public class SamlAccessFilter implements Filter
{
	private static final String BY_PASS_KEY = "native";
	private static final String BY_PASS_VALUE = "true";
	private static final String TEXT_XML = "text/xml";
	public static final String REFERRER_PARAMETER_KEY = "referrer";
	public static final String ORIGINAL_REQUEST = "original_request";
	private final SamlAuthenticationService samlAuthenticationService;
	private final MetaDataXMLPrinter metaDataXMLPrinter;
	private final HostWebAPI hostWebAPI;
	private final LanguageWebAPI languageWebAPI;
	private final PermissionAPI permissionAPI;
	private final IdentifierAPI identifierAPI;
	private final ContentletAPI contentletAPI;
	private final UserWebAPI userWebAPI;
	private final LoginServiceAPI loginService;

	public SamlAccessFilter()
	{
		this( InstanceUtil.newInstance( Config.getStringProperty( DotSamlConstants.DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME, null ), OpenSamlAuthenticationServiceImpl.class ) );
	}

	@VisibleForTesting
	public SamlAccessFilter( final SamlAuthenticationService samlAuthenticationService )
	{
		this( samlAuthenticationService, new MetaDataXMLPrinter(), WebAPILocator.getHostWebAPI(), WebAPILocator.getLanguageWebAPI(), APILocator.getPermissionAPI(), APILocator.getIdentifierAPI(), APILocator.getContentletAPI(), WebAPILocator.getUserWebAPI(), APILocator.getLoginServiceAPI() );
	}

	@VisibleForTesting
	public SamlAccessFilter( final SamlAuthenticationService samlAuthenticationService, final MetaDataXMLPrinter metaDataXMLPrinter, final HostWebAPI hostWebAPI, final LanguageWebAPI languageWebAPI, final PermissionAPI permissionAPI, final IdentifierAPI identifierAPI, final ContentletAPI contentletAPI, final UserWebAPI userWebAPI, final LoginServiceAPI loginService )
	{
		this.samlAuthenticationService = samlAuthenticationService;
		this.metaDataXMLPrinter = metaDataXMLPrinter;
		this.hostWebAPI = hostWebAPI;
		this.languageWebAPI = languageWebAPI;
		this.permissionAPI = permissionAPI;
		this.identifierAPI = identifierAPI;
		this.contentletAPI = contentletAPI;
		this.userWebAPI = userWebAPI;
		this.loginService = loginService;
	}

	@Override
	public void init( final FilterConfig filterConfig ) throws ServletException
	{
		// Do nothing
	}

	/**
	 * This method checks if some path does not wants to be treatment by the
	 * {@link SamlAccessFilter} An example of exception might be the
	 * destroy.jsp, so on.
	 * 
	 * @param uri
	 *            {@link String}
	 * @param filterPaths
	 *            {@link String} array
	 * @return boolean
	 */
	private boolean checkAccessFilters( final String uri, final String[] filterPaths )
	{
		boolean filter = false;

		if ( null != filterPaths )
		{
			for ( String filterPath : filterPaths )
			{
				filter |= uri.contains( filterPath ); //("saml3/metadata/dotcms_metadata.xml")
			}
		}

		return filter;
	}

	/**
	 * Determine if the path is Backend Admin, usually it is for /c && /admin or
	 * if the path is a file or path, will check if the user has permission
	 * 
	 * @param uri
	 *            {@link String}
	 * @param includePaths
	 *            {@link String} array
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return boolean
	 */
	private boolean checkIncludePath( final String uri, final String[] includePaths, final HttpServletRequest request )
	{
		boolean include = false;

		// this is the backend uri test.
		for ( String includePath : includePaths )
		{
			Logger.debug( this, "Evaluating the uri: " + uri + ", with the pattern: " + includePath );

			include |= RegEX.contains( uri, includePath );
		}

		// note: by now we are going to
		/*
		 * if (!include) { try { Logger.debug(this,
		 * "The include paths were not included the uri: " + uri +
		 * ", doing the check file page permission"); include =
		 * this.checkFilePagePermission (uri, request); } catch (Exception e) {
		 * Logger.error(this,
		 * "Unable to check File/Page permission current request host for URI "
		 * + uri); include = false; } }
		 */

		Logger.debug( this, "The uri: " + uri + ", include = " + include );

		return include;
	}

	private boolean isByPass( final HttpServletRequest request, final HttpSession session )
	{
		String byPass = request.getParameter( BY_PASS_KEY );

		if ( null != session )
		{
			if ( null != byPass )
			{
				session.setAttribute( BY_PASS_KEY, byPass );
			}
			else
			{
				if ( this.isNotLogged( request, session ) )
				{
					byPass = (String) session.getAttribute( BY_PASS_KEY );
				}
				else if ( null != session.getAttribute( BY_PASS_KEY ) )
				{
					session.removeAttribute( BY_PASS_KEY );
				}
			}
		}

		return BY_PASS_VALUE.equalsIgnoreCase( byPass );
	}

	@Override
	public void doFilter( final ServletRequest req, final ServletResponse res, final FilterChain chain ) throws IOException, ServletException
	{
		final HttpServletResponse response = (HttpServletResponse) res;
		final HttpServletRequest request = (HttpServletRequest) req;
		HttpSession session = request.getSession();
		String redirectAfterLogin = null;
		boolean isLogoutNeed = false;

		if ( this.isByPass( request, session ) )
		{
			Logger.debug( this, "Using SAML by pass" );
			chain.doFilter( req, res );
			return;
		}

		try
		{
			final IdpConfig idpConfig = SiteIdpConfigResolver.getInstance().resolveIdpConfig( request );

			// If idpConfig is null, means this site does not need SAML processing
			if ( idpConfig != null && idpConfig.isEnabled() )
			{
				isLogoutNeed = idpConfig.getOptionBoolean( DotSamlConstants.DOTCMS_SAML_IS_LOGOUT_NEED, true );

				// First, check if the current request is the SP metadata xml.
				if ( request.getRequestURI().contains( idpConfig.getServiceProviderCustomMetadataPath() ) )
				{
					// if its, so print it out in the response and return.
					this.printMetaData( request, response, idpConfig );
					return;
				}

				// check if there is any exception filter path, to avoid to canApply all the logic.
				if ( !this.checkAccessFilters( request.getRequestURI(), idpConfig.getAccessFilterArray() ) && this.checkIncludePath( request.getRequestURI(), idpConfig.getIncludePathArray(), request ) )
				{
					// if it is an url to canApply the Saml access logic, determine if the autoLogin is possible
					// the autologin will works if the SAMLArt (Saml artifact id) is in the request query string
					// for artifact resolution or SAMLResponse for post resolution.
					final AutoLoginResult autoLoginResult = this.doAutoLogin( request, response, session, idpConfig );

					if ( !autoLoginResult.isAutoLogin() )
					{
						return; // no continue. Usually no continue when there is a sendRedirect or sendError done.
					}

					// we have to assign again the session, since the doAutoLogin might be renewed.
					session = autoLoginResult.getSession();

					// if the auto login couldn't logged the user, then send it to the IdP login page (if it is not already logged in).
					if ( null == session || this.isNotLogged( request, session ) )
					{
						Logger.debug( this, "User is not logged, processing saml request" );
						this.doRequestLoginSecurityLog( request, idpConfig );

						final String originalRequest = request.getRequestURI() + ( ( null != request.getQueryString() ) ? "?" + request.getQueryString() : StringUtils.EMPTY );

						redirectAfterLogin = ( UtilMethods.isSet( request.getParameter( REFERRER_PARAMETER_KEY ) ) ) ? request.getParameter( REFERRER_PARAMETER_KEY ) :
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
							this.samlAuthenticationService.authentication( request, response, idpConfig.getId() );
						}
						catch ( DotSamlException | DotDataException exception )
						{
							Logger.error( this, "Error on authentication: " + exception.getMessage(), exception );
							Logger.debug( this, "Error on authentication, settings 500 response status." );
							response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
						}

						return;
					}
				}

				// Starting the logout
				// if it is logout
				if ( isLogoutNeed && session != null && this.isLogoutRequest( request.getRequestURI(), idpConfig.getLogoutPathArray() ) )
				{
					if ( this.doLogout( response, request, session, idpConfig ) )
					{
						return;
					}
				}

			}
			else if ( idpConfig != null && !idpConfig.isEnabled() )
			{
				// if the idpConfig for this host is not enabled
				// we check if the url is a default metadata url.
				if ( request.getRequestURI().contains( DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE ) )
				{
					// if its, so print it out in the response and return.
					if ( this.printMetaData( request, response, idpConfig ) )
					{
						Logger.debug( this, "Metadata printed" );
						return;
					}
				}

			}
			else
			{
				Logger.debug( this, "No idpConfig for the site: " + request.getServerName() + ". Not any SAML filtering for this request: " + request.getRequestURI() );
			}

		}
		catch ( JSONException | DotDataException exception )
		{
			Logger.error( this, "Error reading idpConfig for the site: " + request.getServerName(), exception );
		}

		chain.doFilter( request, response );

	}

	private boolean doLogout( final HttpServletResponse response, final HttpServletRequest request, final HttpSession session, final IdpConfig idpConfig ) throws IOException, ServletException
	{
		final NameID nameID = (NameID) session.getAttribute( idpConfig.getId() + SamlUtils.SAML_NAME_ID );
		final String samlSessionIndex = (String) session.getAttribute( idpConfig.getId() + SamlUtils.SAML_SESSION_INDEX );
		boolean doLogoutDone = false;

		try
		{
			if ( null != nameID && null != samlSessionIndex )
			{
				Logger.debug( this, "The uri: " + request.getRequestURI() + ", is a logout request. Doing the logout call to saml" );
				Logger.debug( this, "Doing dotCMS logout" );

				doLogout( response, request );
				Logger.debug( this, "Doing SAML redirect logout" );
				this.samlAuthenticationService.logout( request, response, nameID, samlSessionIndex, idpConfig.getId() );
				Logger.info( this, "User " + nameID + " has logged out" );

				doLogoutDone = true;
			}
			else
			{
				Logger.warn( this, "Couldn't do the logout request. Because the saml name id or the saml session index are not in the http session" );
			}
		}
		catch ( Throwable e )
		{
			Logger.error( this, "Error on Logout: " + e.getMessage(), e );
		}

		return doLogoutDone;
	}

	/**
	 * Do the dotCMS logout
	 * 
	 * @param response
	 * @param request
	 */
	private void doLogout( final HttpServletResponse response, final HttpServletRequest request )
	{
		final Cookie[] cookies = request.getCookies();

		if ( cookies != null )
		{
			for ( Cookie cookie : cookies )
			{
				cookie.setMaxAge( 0 );
				cookie.setPath( "/" );
				response.addCookie( cookie );
			}
		}

		HttpSession session = request.getSession( false );

		if ( session != null )
		{
			final Map sessions = PortletSessionPool.remove( session.getId() );

			if ( sessions != null )
			{
				final Iterator itr = sessions.values().iterator();

				while ( itr.hasNext() )
				{
					final HttpSession portletSession = (HttpSession) itr.next();

					if ( portletSession != null )
					{
						portletSession.invalidate();
					}

				}
			}

			if ( !session.isNew() )
			{
				this.loginService.doLogout( request, response );
			}
		}
	}

	private boolean isLogoutRequest( final String requestURI, final String[] logoutPathArray )
	{
		boolean isLogoutRequest = false;

		if ( null != logoutPathArray )
		{
			for ( String logoutPath : logoutPathArray )
			{
				isLogoutRequest |= requestURI.startsWith( logoutPath );
			}
		}

		return isLogoutRequest;
	}

	private void doRequestLoginSecurityLog( final HttpServletRequest request, final IdpConfig idpConfig )
	{
		try
		{
			final Host host = this.hostWebAPI.getCurrentHost( request );
			final String env = this.isFrontEndLoginPage( request.getRequestURI() ) ? "frontend" : "backend";
			final String log = new Date() + ": SAML login request for host: (" + host.getHostname() + ") site: " + idpConfig.getId() + " (" + env + ") from " + request.getRemoteAddr();

			// “$TIMEDATE: SAML login request for $host (frontend|backend)from $REQUEST_ADDR”
			SecurityLogger.logInfo( SecurityLogger.class, SamlAccessFilter.class + " - " + log );
			Logger.debug( this, log );
		}
		catch ( Exception e )
		{
			Logger.error( this, e.getMessage(), e );
		}
	}

	private void doAuthenticationLoginSecurityLog( final HttpServletRequest request, final IdpConfig idpConfig, final User user )
	{
		try
		{
			final Host host = this.hostWebAPI.getCurrentHost( request );
			final String env = this.isFrontEndLoginPage( request.getRequestURI() ) ? "frontend" : "backend";
			final String log = new Date() + ": SAML login success for host: (" + host.getHostname() + ") site: " + idpConfig.getId() + " (" + env + ") from " + request.getRemoteAddr() + " for an user: " + user.getEmailAddress();

			//“$TIMEDATE: SAML login success for $host (frontend|backend)from $REQUEST_ADDR for user $username”
			SecurityLogger.logInfo( SecurityLogger.class, SamlAccessFilter.class + " - " + log );
			Logger.info( this, log );
		}
		catch ( Exception e )
		{
			Logger.error( this, e.getMessage(), e );
		}
	}

	/**
	 * Return true if the user is not logged. Work for FE and BE
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param session
	 *            {@link HttpSession}
	 * @return boolean
	 */
	private boolean isNotLogged( final HttpServletRequest request, final HttpSession session )
	{
		boolean isNotLogged = true;
		boolean isBackend = this.isBackEndAdmin( session, request.getRequestURI() );
		try
		{
			isNotLogged = ( isBackend ) ? !this.userWebAPI.isLoggedToBackend( request ) : null == this.userWebAPI.getLoggedInFrontendUser( request );

			Logger.debug( this, "The user is in backend: " + isBackend + ", is not logged: " + isNotLogged );
		}
		catch ( PortalException | SystemException e )
		{
			Logger.error( this, e.getMessage(), e );
			isNotLogged = true;
		}

		return isNotLogged;
	}

	private boolean printMetaData( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws ServletException
	{
		// First, get the Entity descriptor.
		final EntityDescriptor descriptor = idpConfig.getMetaDescriptorService().getServiceProviderEntityDescriptor( idpConfig );
		Writer writer = null;
		boolean isOK = false;

		try
		{
			Logger.debug( this, "Going to print the descriptor: " + descriptor );
			// get ready to convert it to XML.
			response.setContentType( TEXT_XML );
			writer = response.getWriter();
			this.metaDataXMLPrinter.print( descriptor, writer );
			response.setStatus( HttpServletResponse.SC_OK );
			isOK = true;
			Logger.debug( this, "Descriptor printed..." );
		}
		catch ( ParserConfigurationException | TransformerException | IOException | MarshallingException e )
		{
			Logger.error( this.getClass(), e.getMessage(), e );
			throw new ServletException( e );
		}
		finally
		{
			IOUtils.closeQuietly( writer );
		}

		return isOK;
	}

	private AutoLoginResult doAutoLogin( final HttpServletRequest request, final HttpServletResponse response, final HttpSession session, final IdpConfig idpConfig ) throws  DotDataException, IOException,JSONException
	{
		AutoLoginResult autoLogin = new AutoLoginResult( session, false );

		try
		{
			autoLogin = this.autoLogin( request, response, session, idpConfig );
		}
		catch ( SamlUnauthorizedException e )
		{
			autoLogin = new AutoLoginResult( session, false );
			Logger.debug( this, e.getMessage() );
			Logger.debug( this, "SamlUnauthorizedException, status = " + e.getStatus() );
			Logger.debug( this, "SamlUnauthorizedException, unauthorizedPage = " + e.getUnauthorizedPage() );
			Logger.debug( this, "SamlUnauthorizedException, stackTrace = ", e );

			final int status = e.getStatus() > 0 ? e.getStatus() : HttpServletResponse.SC_UNAUTHORIZED;

			if ( e.getUnauthorizedPage() != null )
			{
				Logger.debug( this, "SamlUnauthorizedException, doing redirection with status: " + e.getStatus() + ", to the page: " + e.getUnauthorizedPage() );

				response.setStatus( e.getStatus() );
				response.sendRedirect( e.getUnauthorizedPage() );
			}
			else
			{
				Logger.debug( this, "SamlUnauthorizedException, doing redirection error with status: " + e.getStatus() );
				response.sendError( status );
			}
		}
		catch ( NotNullEmailAllowedException e )
		{
			autoLogin = new AutoLoginResult( session, false );
			Logger.debug( this, e.getMessage() );

			final int status = e.getStatus() > 0 ? e.getStatus() : HttpServletResponse.SC_UNAUTHORIZED;

			Logger.debug( this, "NotNullEmailAllowedException, doing redirection error with status: " + e.getStatus() );
			response.sendError( status );
		}

		return autoLogin;
	}

	private AutoLoginResult autoLogin( final HttpServletRequest request, final HttpServletResponse response, final HttpSession session, final IdpConfig idpConfig ) throws DotDataException, IOException, JSONException
	{

		final User user = this.samlAuthenticationService.getUser( request, response, session, idpConfig.getId() );
		boolean continueFilter = true; // by default continue with the filter
		HttpSession renewSession = session;

		if ( null != user )
		{
			// we are going to do the autologin, so if the session is null, create it!
			try
			{
				Logger.debug( this, "User returned by SAML Service, id " + user.getUserId() + ", user Map: " + user.toMap() );
			}
			catch ( Exception e )
			{
				Logger.error( this, e.getMessage(), e );
			}

			final boolean doCookieLogin = this.loginService.doCookieLogin( PublicEncryptionFactory.encryptString( user.getUserId() ), request, response );

			Logger.debug( this, "Login result by LoginService: " + doCookieLogin );

			if ( doCookieLogin )
			{
				if ( null != session && null != user.getUserId() )
				{
					// this is what the PortalRequestProcessor needs to check the login.
					Logger.debug( this, "Setting the user id on the session: " + user.getUserId() );

					final String uri = session.getAttribute( ORIGINAL_REQUEST ) != null ? (String) session.getAttribute( ORIGINAL_REQUEST ) : request.getRequestURI();
					session.removeAttribute( ORIGINAL_REQUEST );

					if ( this.isBackEndAdmin( session, uri ) )
					{
						Logger.debug( this, "The uri: " + uri + ", is a backend setting the session backend stuff" );
						session.setAttribute( com.liferay.portal.util.WebKeys.USER_ID, user.getUserId() );
						PrincipalThreadLocal.setName( user.getUserId() );
					}

					renewSession = this.renewSession( request, session );

					// depending if it is a redirection or not, continue.
					continueFilter = this.checkRedirection( request, response, renewSession );

					this.doAuthenticationLoginSecurityLog( request, idpConfig, user );
				}
			}
		}
		else
		{
			// if it was a saml request and could not get the user, throw an error
			if ( this.samlAuthenticationService.isValidSamlRequest( request, response, idpConfig.getId() ) )
			{
				Logger.error( this, "This request is a saml request, but couldn't resolve the user so throwing an internal error" );
				response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
				continueFilter = false; // not continue. since it is an error redirect.
			}
		}

		return new AutoLoginResult( renewSession, continueFilter );
	}

	private HttpSession renewSession( final HttpServletRequest request, HttpSession currentSession )
	{
		String attributeName = null;
		Object attributeValue = null;
		Enumeration<String> attributesNames = null;
		HttpSession renewSession = currentSession;
		final Map<String, Object> sessionAttributes = new HashMap<>();

		if ( null != currentSession && !currentSession.isNew() )
		{
			Logger.debug( this, "Starting the Renew of the current session" );

			attributesNames = currentSession.getAttributeNames();

			while ( attributesNames.hasMoreElements() )
			{
				attributeName = attributesNames.nextElement();
				attributeValue = currentSession.getAttribute( attributeName );
				Logger.debug( this, "Copying the attribute: " + attributeName );
				sessionAttributes.put( attributeName, attributeValue );
			}

			Logger.debug( this, "Killing the current session" );
			currentSession.invalidate(); // kill the previous session

			Logger.debug( this, "Creating a new session" );
			renewSession = request.getSession( true );

			for ( Map.Entry<String, Object> sessionEntry : sessionAttributes.entrySet() )
			{
				Logger.debug( this, "Setting the attribute to the new session: " + sessionEntry.getKey() );
				renewSession.setAttribute( sessionEntry.getKey(), sessionEntry.getValue() );
			}

		}

		return renewSession;
	}

	private boolean isBackEndAdmin( final HttpSession session, final String uri )
	{
		return ( session.getAttribute( com.dotmarketing.util.WebKeys.ADMIN_MODE_SESSION ) != null ) ||
		// todo: on higher versions this hack should be checked for a better criteria
		this.isBackEndLoginPage( uri );
	}

	private boolean isBackEndLoginPage( final String uri )
	{
		return uri.startsWith( "/dotAdmin" ) || uri.startsWith( "/html/portal/login" ) || uri.startsWith( "/c/public/login" ) || uri.startsWith( "/c/portal_public/login" ) || uri.startsWith( "/c/portal/logout" );
	}

	private boolean isFrontEndLoginPage( final String uri )
	{
		return uri.startsWith( "/dotCMS/login" ) || uri.startsWith( "/application/login" );
	}

	private boolean checkRedirection( final HttpServletRequest request, final HttpServletResponse response, final HttpSession session )
	{
		final String redirectAfterLogin = (String) session.getAttribute( WebKeys.REDIRECT_AFTER_LOGIN );

		if ( null != redirectAfterLogin )
		{
			session.removeAttribute( WebKeys.REDIRECT_AFTER_LOGIN );
			final String currentRequest = request.getRequestURI() + ( ( null != request.getQueryString() ) ? "?" + request.getQueryString() : StringUtils.EMPTY );

			// if it is not the same request.
			if ( !currentRequest.equals( redirectAfterLogin ) )
			{
				try
				{
					if ( this.isBackEndLoginPage( redirectAfterLogin ) || this.isFrontEndLoginPage( redirectAfterLogin ) )
					{
						if ( this.isBackEndAdmin( session, redirectAfterLogin ) )
						{
							Logger.debug( this, "Redirecting to: /dotAdmin" );
							response.sendRedirect( "/dotAdmin" );
						}
						else
						{
							// if it is front end
							Logger.debug( this, "Redirecting to: /" );
							response.sendRedirect( "/" );
						}
					}
					else
					{
						Logger.debug( this, "Redirecting to: " + redirectAfterLogin );
						response.sendRedirect( redirectAfterLogin );
					}

					return false; // not continue. since it is a redirect.
				}
				catch ( IOException e )
				{
					Logger.error( this, e.getMessage(), e );
				}
			}
		}

		// continue with the current request.
		return true;
	}

	@Override
	public void destroy()
	{

	}
}
