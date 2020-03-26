package com.dotcms.plugin.saml.v3.filter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.dotmarketing.util.PageMode;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import com.dotcms.cms.login.LoginServiceAPI;
import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.MetaDataHelper;
import com.dotcms.plugin.saml.v3.init.Initializer;
import com.dotcms.plugin.saml.v3.init.SamlInitializer;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.service.OpenSamlAuthenticationServiceImpl;
import com.dotcms.plugin.saml.v3.service.SamlAuthenticationService;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;
import com.dotcms.plugin.saml.v3.util.MetaDataXMLPrinter;
import com.dotcms.plugin.saml.v3.util.SamlUtils;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
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
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.RegEX;
import com.dotmarketing.util.SecurityLogger;
import com.dotmarketing.util.json.JSONException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.auth.PrincipalThreadLocal;
import com.liferay.portal.model.User;
import com.liferay.portal.servlet.PortletSessionPool;

public class SamlFilter implements Filter {
	protected static final String BY_PASS_KEY = "native";
	protected static final String BY_PASS_VALUE = "true";
	protected static final String TEXT_XML = "text/xml";
	public static final String REFERRER_PARAMETER_KEY = "referrer";
	public static final String ORIGINAL_REQUEST = "original_request";
	public final SamlAuthenticationService samlAuthenticationService;
	protected final MetaDataXMLPrinter metaDataXMLPrinter;
	protected final HostWebAPI hostWebAPI;
	protected final LanguageWebAPI languageWebAPI;
	protected final PermissionAPI permissionAPI;
	protected final IdentifierAPI identifierAPI;
	protected final ContentletAPI contentletAPI;
	protected final UserWebAPI userWebAPI;
	protected final LoginServiceAPI loginService;
	protected final Initializer initializer = new SamlInitializer();

	public SamlFilter() {
		this(InstanceUtil.newInstance(
				Config.getStringProperty(DotSamlConstants.DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME, null),
				OpenSamlAuthenticationServiceImpl.class));
	}

	@VisibleForTesting
	public SamlFilter(final SamlAuthenticationService samlAuthenticationService) {
		this(samlAuthenticationService, new MetaDataXMLPrinter(), WebAPILocator.getHostWebAPI(),
				WebAPILocator.getLanguageWebAPI(), APILocator.getPermissionAPI(), APILocator.getIdentifierAPI(),
				APILocator.getContentletAPI(), WebAPILocator.getUserWebAPI(), APILocator.getLoginServiceAPI());
	}

	@VisibleForTesting
	public SamlFilter(final SamlAuthenticationService samlAuthenticationService,
			final MetaDataXMLPrinter metaDataXMLPrinter, final HostWebAPI hostWebAPI,
			final LanguageWebAPI languageWebAPI, final PermissionAPI permissionAPI, final IdentifierAPI identifierAPI,
			final ContentletAPI contentletAPI, final UserWebAPI userWebAPI, final LoginServiceAPI loginService) {
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
	public void init(final FilterConfig filterConfig) throws ServletException {
		Logger.debug(this, "Calling the SAML Filter Initializer: " + this.initializer);

		if (!this.initializer.isInitializationDone()) {
			try {
				this.initializer.init(Collections.emptyMap());
			} catch (final Throwable e) {
				Logger.error(this, "An error occurred when initializing the SAML Filter: " + e.getMessage(), e);
			}
		} else {
			Logger.debug(this, "The SAML Filter initializer was already initialized: " + this.initializer);
		}
	}

	/**
	 * This method checks if some path does not wants to be treatment by the
	 * {@link SamlFilter} An example of exception might be the destroy.jsp, so
	 * on.
	 * 
	 * @param uri
	 *            {@link String}
	 * @param filterPaths
	 *            {@link String} array
	 * @return boolean
	 */
	protected boolean checkAccessFilters(final String uri, final String[] filterPaths) {
		boolean filter = false;

		if (null != filterPaths) {
			for (String filterPath : filterPaths) {
				filter |= uri.contains(filterPath);
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
	protected boolean checkIncludePath(final String uri, final String[] includePaths,
			final HttpServletRequest request) {
		boolean include = false;

		// this is the backend uri test.
		for (String includePath : includePaths) {
			Logger.debug(this, "Evaluating URI '" + uri + "' with pattern: " + includePath);

			include |= RegEX.contains(uri, includePath);
		}

		Logger.debug(this, "Incoming URI '" + uri + "', include? " + include);

		return include;
	}

	protected boolean isByPass(final HttpServletRequest request, final HttpSession session) {
		String byPass = request.getParameter(BY_PASS_KEY);

		if (null != session) {
			if (null != byPass) {
				session.setAttribute(BY_PASS_KEY, byPass);
			} else {
				if (this.isNotLogged(request, session)) {
					byPass = (String) session.getAttribute(BY_PASS_KEY);
				} else if (null != session.getAttribute(BY_PASS_KEY)) {
					session.removeAttribute(BY_PASS_KEY);
				}
			}
		}

		return BY_PASS_VALUE.equalsIgnoreCase(byPass);
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
			final FilterChain filterChain) throws IOException, ServletException {
		final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

	public boolean doLogout(final HttpServletResponse response, final HttpServletRequest request,
			final HttpSession session, final IdpConfig idpConfig) throws IOException, ServletException {
        Logger.debug(this, "------------------------------ IdP doLogout ---------------------------------");
		final NameID nameID = (NameID) session.getAttribute(idpConfig.getId() + SamlUtils.SAML_NAME_ID);
		final String samlSessionIndex = (String) session.getAttribute(idpConfig.getId() + SamlUtils.SAML_SESSION_INDEX);
		boolean doLogoutDone = false;
        Logger.debug(this, "- NameID = " + nameID);
        Logger.debug(this, "- samlSessionIndex = " + samlSessionIndex);
		try {
			if (null != nameID && null != samlSessionIndex) {
				Logger.debug(this,
						"The URI '" + request.getRequestURI() + "' is a logout request. Executing the logout call to SAML");
				Logger.debug(this, "Executing dotCMS logout");

				doLogout(response, request);
				Logger.debug(this, "Executing SAML redirect logout");
				this.samlAuthenticationService.logout(request, response, nameID, samlSessionIndex, idpConfig);
				Logger.info(this, "User '" + nameID + "' has logged out");

				doLogoutDone = true;
			} else {
				Logger.warn(this, "Couldn't execute the logout request. The SAML NameID or the SAML session index are not " +
						"in the HTTP session");
			}
		} catch (Throwable e) {
			Logger.error(this, "Error on Logout: " + e.getMessage(), e);
		}
        Logger.debug(this, "- doLogoutDone = " + doLogoutDone);
		return doLogoutDone;
	}

	/**
	 * Do the dotCMS logout
	 * 
	 * @param response
	 * @param request
	 */
	protected void doLogout(final HttpServletResponse response, final HttpServletRequest request) {
        Logger.debug(this, "---------------------------- Generic doLogout -------------------------------");
		final Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cookie.setMaxAge(0);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}
        Logger.debug(this, "- Removing cookies...");
		HttpSession session = request.getSession(false);
        Logger.debug(this, "- Invalidating session...");
		if (session != null) {
            Logger.debug(this, "- Session IS NOT null. Invalidating session maps...");
			final Map sessions = PortletSessionPool.remove(session.getId());

			if (sessions != null) {
				final Iterator itr = sessions.values().iterator();

				while (itr.hasNext()) {
					final HttpSession portletSession = (HttpSession) itr.next();

					if (portletSession != null) {
						portletSession.invalidate();
					}

				}
			}

			if (!session.isNew()) {
                Logger.debug(this, "- Logging out through the dotCMS Login Service...");
				this.loginService.doLogout(request, response);
			}
		}
	}

	public boolean isLogoutRequest(final String requestURI, final String[] logoutPathArray) {
		Logger.debug(this, "----------------------------- isLogoutRequest --------------------------------");
		Logger.debug(this, "- requestURI = " + requestURI);
        Logger.debug(this, "- logoutPathArray = " + Arrays.asList(logoutPathArray));
		boolean isLogoutRequest = false;

		if (null != logoutPathArray) {
			for (String logoutPath : logoutPathArray) {
				if (requestURI.startsWith(logoutPath) || requestURI.equals(logoutPath)) {
					isLogoutRequest = true;
				}
			}
		}
        Logger.debug(this, "- isLogoutRequest = " + isLogoutRequest);
		return isLogoutRequest;
	}

	public void doRequestLoginSecurityLog(final HttpServletRequest request, final IdpConfig idpConfig) {
		try {
			final Host host = this.hostWebAPI.getCurrentHost(request);
			final String env = this.isFrontEndLoginPage(request.getRequestURI()) ? "frontend" : "backend";
			final String log = new Date() + ": SAML login request for Site '" + host.getHostname() + "' with IdP ID: "
					+ idpConfig.getId() + " (" + env + ") from " + request.getRemoteAddr();

			// “$TIMEDATE: SAML login request for $host (frontend|backend)from
			// $REQUEST_ADDR”
			SecurityLogger.logInfo(SecurityLogger.class, SamlFilter.class + " - " + log);
			Logger.debug(this, log);
		} catch (Exception e) {
			Logger.error(this, e.getMessage(), e);
		}
	}

	protected void doAuthenticationLoginSecurityLog(final HttpServletRequest request, final IdpConfig idpConfig,
			final User user) {
		try {
			final Host host = this.hostWebAPI.getCurrentHost(request);
			final String env = this.isFrontEndLoginPage(request.getRequestURI()) ? "frontend" : "backend";
			final String log = new Date() + ": Successfull SAML login for Site '" + host.getHostname() + "' with IdP " +
					"ID: " + idpConfig.getId() + " (" + env + ") from " + request.getRemoteAddr() + " for user: " +
					user.getEmailAddress();

			// “$TIMEDATE: SAML login success for $host (frontend|backend)from
			// $REQUEST_ADDR for user $username”
			SecurityLogger.logInfo(SecurityLogger.class, SamlFilter.class + " - " + log);
			Logger.info(this, log);
		} catch (Exception e) {
			Logger.error(this, e.getMessage(), e);
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
	protected boolean isNotLogged(final HttpServletRequest request, final HttpSession session) {
		boolean isNotLogged = true;
		boolean isBackend = this.isBackEndAdmin(session, request.getRequestURI());
		try {
			isNotLogged = (isBackend) ? !this.userWebAPI.isLoggedToBackend(request)
					: null == this.userWebAPI.getLoggedInFrontendUser(request);

			Logger.debug(this, "Trying to go to back-end login? " + isBackend + ", Is user NOT logged in? " + isNotLogged);
		} catch (PortalException | SystemException e) {
			Logger.error(this, e.getMessage(), e);
			isNotLogged = true;
		}

		return isNotLogged;
	}

	public boolean printMetaData(final HttpServletRequest request, final HttpServletResponse response,
			final IdpConfig idpConfig) throws ServletException {
		// First, get the Entity descriptor.
		final EntityDescriptor descriptor = MetaDataHelper.getMetaDescriptorService(idpConfig)
				.getServiceProviderEntityDescriptor(idpConfig);
		Writer writer = null;
		boolean isOK;

		try {
			Logger.debug(this, "Printing Metadata Descriptor:");
			Logger.debug(this, "\n\n" + descriptor);
			// get ready to convert it to XML.
			response.setContentType(TEXT_XML);
			writer = response.getWriter();
			this.metaDataXMLPrinter.print(descriptor, writer);
			response.setStatus(HttpServletResponse.SC_OK);
			isOK = true;
			Logger.debug(this, "Metadata Descriptor printed.");
		} catch (ParserConfigurationException | TransformerException | IOException | MarshallingException e) {
			Logger.error(this.getClass(), e.getMessage(), e);
			throw new ServletException(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}

		return isOK;
	}

	protected AutoLoginResult autoLogin(final HttpServletRequest request, final HttpServletResponse response,
			final HttpSession session, final IdpConfig idpConfig) throws DotDataException, IOException, JSONException {
		User user = this.samlAuthenticationService.getUser(request, idpConfig);
		boolean continueFilter = true; // by default continue with the filter
		HttpSession renewSession = session;

		if (null != user) {
			// we are going to do the autologin, so if the session is null,
			// create it!
			try {
				Logger.debug(this, "User with ID '" + user.getUserId() + "' has been returned by SAML Service. User " +
						"Map: " + user.toMap());
			} catch (Exception e) {
				Logger.error(this, "An error occurred when retrieving data from user '" + user.getUserId() + "': " + e
						.getMessage(), e);
			}

			final boolean doCookieLogin = this.loginService
					.doCookieLogin(PublicEncryptionFactory.encryptString(user.getUserId()), request, response);

			Logger.debug(this, "Cookie Login by LoginService = " + doCookieLogin);

			if (doCookieLogin) {
				if (null != session && null != user.getUserId()) {
					// this is what the PortalRequestProcessor needs to check
					// the login.
					Logger.debug(this, "Adding user ID '" + user.getUserId() + "' to the session");

					final String uri = session.getAttribute(ORIGINAL_REQUEST) != null
							? (String) session.getAttribute(ORIGINAL_REQUEST) : request.getRequestURI();
					session.removeAttribute(ORIGINAL_REQUEST);

					if (this.isBackEndAdmin(session, uri)) {
						Logger.debug(this, "URI '" + uri + "' belongs to the back-end. Setting the user session data");
						session.setAttribute(com.liferay.portal.util.WebKeys.USER_ID, user.getUserId());
						PrincipalThreadLocal.setName(user.getUserId());
					}

					renewSession = this.renewSession(request, session);

					this.doAuthenticationLoginSecurityLog(request, idpConfig, user);
				}
			}
		}

		return new AutoLoginResult(renewSession, continueFilter);
	}

	protected HttpSession renewSession(final HttpServletRequest request, HttpSession currentSession) {
		String attributeName = null;
		Object attributeValue = null;
		Enumeration<String> attributesNames = null;
		HttpSession renewSession = currentSession;
		final Map<String, Object> sessionAttributes = new HashMap<>();

		if (null != currentSession && !currentSession.isNew()) {
			Logger.debug(this, "Renewing the HTTP session");

			attributesNames = currentSession.getAttributeNames();

			while (attributesNames.hasMoreElements()) {
				attributeName = attributesNames.nextElement();
				attributeValue = currentSession.getAttribute(attributeName);
				Logger.debug(this, "Copying attribute '" + attributeName + "' to the new session.");
				sessionAttributes.put(attributeName, attributeValue);
			}

			Logger.debug(this, "Killing the current session");
			currentSession.invalidate(); // kill the previous session

			Logger.debug(this, "Creating a new one");
			renewSession = request.getSession(true);

			for (Map.Entry<String, Object> sessionEntry : sessionAttributes.entrySet()) {
				Logger.debug(this, "Adding attribute '" + sessionEntry.getKey() + "' to the new session.");
				renewSession.setAttribute(sessionEntry.getKey(), sessionEntry.getValue());
			}

		}

		return renewSession;
	}

	/**
	 * Determines whether the user in the {@link HttpSession} object or the incoming URI belong to the
	 * dotCMS back-end login mechanism or not.
	 *
	 * @param session The {@link HttpSession} object containing user information.
	 * @param uri     The incoming URI for login.
	 *
	 * @return If the user or its URI can be associated to the dotCMS back-end login, returns {@code true}. Otherwise,
	 * returns {@code false}.
	 */
	protected boolean isBackEndAdmin(final HttpSession session, final String uri) {
		return PageMode.get(session).isAdmin || this.isBackEndLoginPage(uri);
	}

	/**
	 * Analyzes the incoming URI and determines whether it belongs to dotCMS back-end login or logout URIs or not.
	 *
	 * @param uri The incoming URI.
	 *
	 * @return If the URI can be associated to the dotCMS back-end login or logout, returns {@code true}. Otherwise,
	 * returns {@code false}.
	 */
	protected boolean isBackEndLoginPage(final String uri) {
		return uri.startsWith("/dotAdmin") || uri.startsWith("/html/portal/login") || uri.startsWith("/c/public/login")
				|| uri.startsWith("/c/portal_public/login") || uri.startsWith("/c/portal/logout");
	}

	protected boolean isFrontEndLoginPage(final String uri) {
		return uri.startsWith("/dotCMS/login") || uri.startsWith("/application/login");
	}

	@Override
	public void destroy() {

	}

}
