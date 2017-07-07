package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.plugin.saml.v3.*;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.init.DefaultInitializer;
import com.dotcms.plugin.saml.v3.init.Initializer;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.business.Permissionable;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.LanguageWebAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.cms.login.factories.LoginFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.filters.CmsUrlUtil;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.util.*;
import com.dotmarketing.velocity.VelocityServlet;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.auth.PrincipalThreadLocal;
import com.liferay.portal.model.User;
import com.liferay.util.InstancePool;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.dotmarketing.business.PermissionAPI.PERMISSION_READ;

/**
 * Access filter for SAML plugin, it does the autologin and also redirect to the
 * IDP if the user is not logged in.
 * In addition prints out the metadata.xml information for the dotCMS SP.
 * @author jsanca
 */
public class SamlAccessFilter implements Filter {

    private static final String TEXT_XML = "text/xml";
    public static final String REFERRER_PARAMETER_KEY = "referrer";
    public static final String ORIGINAL_REQUEST = "original_request";
    private final SamlAuthenticationService samlAuthenticationService;
    private final Initializer initializer;
    private final MetaDataXMLPrinter metaDataXMLPrinter;
    private final HostWebAPI hostWebAPI;
    private final CmsUrlUtil urlUtil;
    private final LanguageWebAPI languageWebAPI;
    private final PermissionAPI permissionAPI;
    private final IdentifierAPI identifierAPI;
    private final ContentletAPI contentletAPI;
    private final UserWebAPI    userWebAPI;

    public SamlAccessFilter() {

        this(InstanceUtil.newInstance(Config.getStringProperty(
                        DotSamlConstants.DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME, null
                ), OpenSamlAuthenticationServiceImpl.class)
                , InstanceUtil.newInstance(Config.getStringProperty(
                        DotSamlConstants.DOT_SAML_INITIALIZER_CLASS_NAME, null
                ), DefaultInitializer.class));
    }

    @VisibleForTesting
    public SamlAccessFilter(final SamlAuthenticationService samlAuthenticationService,
                            final Initializer initializer) {

        this (samlAuthenticationService,
                (null == initializer)?
                    new DefaultInitializer():initializer,
                new MetaDataXMLPrinter(),
                WebAPILocator.getHostWebAPI(),
                CmsUrlUtil.getInstance(),
                WebAPILocator.getLanguageWebAPI(),
                APILocator.getPermissionAPI(),
                APILocator.getIdentifierAPI(),
                APILocator.getContentletAPI(),
                WebAPILocator.getUserWebAPI());

    }

    @VisibleForTesting
    public SamlAccessFilter(final SamlAuthenticationService samlAuthenticationService,
                            final Initializer initializer,
                            final MetaDataXMLPrinter metaDataXMLPrinter,
                            final HostWebAPI hostWebAPI,
                            final CmsUrlUtil urlUtil,
                            final LanguageWebAPI languageWebAPI,
                            final PermissionAPI permissionAPI,
                            final IdentifierAPI identifierAPI,
                            final ContentletAPI contentletAPI,
                            final UserWebAPI    userWebAPI) {

        this.samlAuthenticationService = samlAuthenticationService;
        this.initializer               = initializer;
        this.metaDataXMLPrinter        = metaDataXMLPrinter;
        this.hostWebAPI                = hostWebAPI;
        this.urlUtil                   = urlUtil;
        this.languageWebAPI            = languageWebAPI;
        this.permissionAPI             = permissionAPI;
        this.identifierAPI             = identifierAPI;
        this.contentletAPI             = contentletAPI;
        this.userWebAPI                = userWebAPI;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        Logger.info(this, "Going to call the Initializer: " + this.initializer);

        if (!this.initializer.isInitializationDone()) {

            this.initializer.init(Collections.EMPTY_MAP);
        } else {

            Logger.info(this, "The initializer was already init: " + this.initializer);
        }
    } // init.

    /**
     * This method checks if some path does not wants to be treatment by the {@link SamlAccessFilter}
     * An example of exception might be the destroy.jsp, so on.
     * @param uri {@link String}
     * @param filterPaths {@link String} array
     *
     * @return boolean
     */
    private boolean checkAccessFilters (final String uri, final String [] filterPaths) {

        boolean filter = false;

        if (null != filterPaths) {

            for (String filterPath : filterPaths) {

                filter |= uri.contains(filterPath); //("saml3/metadata/dotcms_metadata.xml")
            }
        }

        return filter;
    } // checkAccessFilters.

    /**
     * Determine if the path is Backend Admin, usually it is for /c && /admin or
     * if the path is a file or path, will check if the user has permission
     * @param uri {@link String}
     * @param includePaths {@link String} array
     * @param request {@link HttpServletRequest}
     * @return boolean
     */
    private boolean checkIncludePath(final String uri, final String [] includePaths, final HttpServletRequest request) {

        boolean include = false;

        // this is the backend uri test.
        for (String includePath : includePaths) {

            Logger.debug(this, "Evaluating the uri: " + uri +
                    ", with the pattern: " + includePath);

            include |= RegEX.contains(uri, includePath);
        }

        // note: by now we are going to
        /*if (!include) {

            try {

                Logger.debug(this, "The include paths were not included the uri: " + uri
                                + ", doing the check file page permission");
                include = this.checkFilePagePermission (uri, request);
            } catch (Exception e) {

                Logger.error(this, "Unable to check File/Page permission current request host for URI " + uri);
                include = false;
            }
        }*/

        Logger.debug(this, "The uri: " + uri + ", include = " + include);

        return include;
    }

    /**
     * If the url represents a file or page, will check if the user needs a permission to access it.
     * It is for the Front end logic.
     * @param uriParam String
     * @return boolean
     */
    private boolean checkFilePagePermission(final String uriParam,
                                            final HttpServletRequest request) throws Exception {

        boolean include             = false;
        String uri                  = URLDecoder.decode(uriParam, UtilMethods.getCharsetConfiguration());
        Host host                   = this.hostWebAPI.getCurrentHost(request);
        Identifier identifier       = null;
        Permissionable contentlet   = null;
        final long languageId       = this.languageWebAPI.getLanguage(request).getId();

        if (this.urlUtil.isFileAsset(uri, host, languageId)
                || this.urlUtil.isPageAsset(uri, host, languageId)) {

            Logger.debug(this, "The uri: " + uri + ", for the site: " + host + ", language: " + languageId +
                    ". Is a contentlet or file asset");
            try {

                identifier = this.identifierAPI.find(host, uri);
                contentlet = this.contentletAPI.findContentletForLanguage(languageId, identifier);
            } catch(DotDataException | DotSecurityException e) {

                Logger.info(VelocityServlet.class,
                        "Unable to find live version of contentlet. Identifier: " + identifier.getId());
                throw new ResourceNotFoundException
                        (String.format("Resource %s not found in Live mode!", uri));
            }

            // Check if the contentlet is visible by a CMS Anonymous role
            include = !this.permissionAPI.doesUserHavePermission(contentlet,
                                PERMISSION_READ, null, true);

            Logger.debug(this, "The uri: " + uri + ", for the site: " + host + ", language: " + languageId +
                    ", with the identifier: " + identifier + " is included: " + include
            );
        }

        return include;
    } // checkFilePagePermission.

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {

        final HttpServletResponse       response      = (HttpServletResponse) res;
        final HttpServletRequest        request       = (HttpServletRequest) req;
        final HttpSession               session       = request.getSession();
        final SiteConfigurationResolver resolver      = (SiteConfigurationResolver)InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration             configuration = resolver.resolveConfiguration(request);
        String redirectAfterLogin                     = null;
        boolean isLogoutNeed                          = false;

        // If configuration is not, means this site does not need SAML processing
        if (null != configuration) {

            isLogoutNeed                                      = configuration.getBooleanProperty
                    (DotSamlConstants.DOTCMS_SAML_IS_LOGOUT_NEED, true);
            ThreadLocalConfiguration.setCurrentSiteConfiguration(configuration);

            // First, check if the current request is the SP metadata xml.
            if (request.getRequestURI().contains(configuration.getServiceProviderCustomMetadataPath())) {

                // if its, so print it out in the response and return.
                this.printMetaData(request, response, configuration);
                return;
            }

            // check if there is any exception filter path, to avoid to canApply all the logic.
            if (!this.checkAccessFilters(request.getRequestURI(), configuration.getAccessFilterArray())
                    && this.checkIncludePath(request.getRequestURI(), configuration.getIncludePathArray(), request)) {

                // if it is an url to canApply the Saml access logic, determine if the autoLogin is possible
                // the autologin will works if the SAMLArt (Saml artifact id) is in the request query string
                // for artifact resolution or SAMLResponse for post resolution.
                if (!this.autoLogin(request, response, session, configuration)) {

                    return; // no continue. Usually no continue when there is a sendRedirect or sendError done.
                }

                // if the auto login couldn't logged the user, then send it to the IdP login page (if it is not already logged in).
                if (null == session || this.isNotLogged(request, session)) {

                    Logger.debug(this, "User is not logged, processing saml request");
                    this.doRequestLoginSecurityLog(request, configuration);

                    final String originalRequest = request.getRequestURI() +
                        ((null != request.getQueryString()) ? "?" + request.getQueryString() :
                            StringUtils.EMPTY);

                    redirectAfterLogin = (UtilMethods.isSet(request.getParameter(REFERRER_PARAMETER_KEY)))?
                            request.getParameter(REFERRER_PARAMETER_KEY):
                            // this is safe, just to make a redirection when the user get's logged.
                            originalRequest;

                    Logger.warn(this.getClass(),
                            "Doing Saml Login Redirection when request: " +
                                    redirectAfterLogin);

                    //if we don't have a redirect yet
                    if (null != session) {

                        session.setAttribute(WebKeys.REDIRECT_AFTER_LOGIN,
                                redirectAfterLogin);
                        session.setAttribute(ORIGINAL_REQUEST, originalRequest);
                    }

                    // this will redirect the user to the IdP Login Page.
                    this.samlAuthenticationService.authentication(request,
                            response, configuration.getSiteName());
                    return;
                }
            }
        } else {

            Logger.debug(this, "Not configuration for the site: " + request.getServerName() +
                            ". No any saml filtering for this request: " + request.getRequestURI());
        }

        // Starting the logout
        // if it is logout
        if (isLogoutNeed && null != session && this.isLogoutRequest(request.getRequestURI(), configuration.getLogoutPathArray())) {

            final NameID nameID           = (NameID)session.getAttribute(configuration.getSiteName() + SamlUtils.SAML_NAME_ID);
            final String samlSessionIndex = (String)session.getAttribute(configuration.getSiteName() + SamlUtils.SAML_SESSION_INDEX);
            if ( null != nameID && null != samlSessionIndex) {

                Logger.info(this, "The uri: " + request.getRequestURI() +
                        ", is a logout request. Doing the logout call to saml");
                Logger.info(this, "Doing dotCMS logout");
                doLogout(request, response);
                Logger.info(this, "Doing SAML redirect logout");
                this.samlAuthenticationService.logout(request,
                        response, nameID, samlSessionIndex, configuration.getSiteName());
                return;
            } else {

                Logger.warn(this,
                        "Couldn't do the logout request. Because the saml name id or the saml session index are not in the http session");
            }
        }

        chain.doFilter(request, response);

    } // doFilter.

	/**
	 * 
	 * @param response
	 * @param request
	 */
	private void doLogout(final HttpServletResponse response, final HttpServletRequest request) {
		final javax.servlet.http.Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				cookie.setMaxAge(0);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}
		HttpSession session = request.getSession(false);
		if (null != session) {
			final Map sessions = PortletSessionPool.remove(session.getId());
			if (null != sessions) {
				final Iterator itr = sessions.values().iterator();
				while (itr.hasNext()) {
					final HttpSession portletSession = (HttpSession) itr.next();
					if (null != portletSession) {

						portletSession.invalidate();
					}
				}
			}
		}
		LoginFactory.doLogout(request, response);
	}

    private boolean isLogoutRequest(final String requestURI, final String[] logoutPathArray) {

        boolean isLogoutRequest = false;

        if (null != logoutPathArray) {

            for (String logoutPath : logoutPathArray) {

                isLogoutRequest |= requestURI.startsWith(logoutPath);
            }
        }

        return isLogoutRequest;
    } // isLogoutRequest.

    private void doRequestLoginSecurityLog(final HttpServletRequest request, final Configuration configuration) {

        try {

            final Host  host    = this.hostWebAPI.getCurrentHost(request);
            final String env    = this.isFrontEndLoginPage(request.getRequestURI())?
                    "frontend":"backend";
            final String log    = new Date() + ": SAML login request for host: (" +
                    host.getHostname()    + ") site: " +
                    configuration.getSiteName() + " (" + env + ") from " +
                    request.getRemoteAddr();

            //“$TIMEDATE: SAML login request for $host (frontend|backend)from $REQUEST_ADDR”
            SecurityLogger.logInfo(SamlAccessFilter.class, log);
            Logger.debug(this, log);
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
        }
    } // doRequestLoginSecurityLog.

    private void doAuthenticationLoginSecurityLog(final HttpServletRequest request,
                                                  final Configuration configuration,
                                                  final User user) {

        try {

            final Host  host    = this.hostWebAPI.getCurrentHost(request);
            final String env    = this.isFrontEndLoginPage(request.getRequestURI())?
                    "frontend":"backend";
            final String log    = new Date() + ": SAML login success for host: (" +
                    host.getHostname()    + ") site: " +
                    configuration.getSiteName() + " (" + env + ") from " +
                    request.getRemoteAddr() + " for an user: " + user.getEmailAddress();

            //“$TIMEDATE: SAML login success for $host (frontend|backend)from $REQUEST_ADDR for user $username”
            SecurityLogger.logInfo(SamlAccessFilter.class, log);
            Logger.debug(this, log);
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
        }
    } // doAuthenticationLoginSecurityLog.

    /**
     * Return true if the user is not logged.
     * Work for FE and BE
     * @param request {@link HttpServletRequest}
     * @param session {@link HttpSession}
     * @return boolean
     */
    private boolean isNotLogged(final HttpServletRequest request, final HttpSession session) {

        boolean isNotLogged = true;
        boolean isBackend   = this.isBackEndAdmin(session, request.getRequestURI());
        try {

            isNotLogged = (isBackend)?
                    !this.userWebAPI.isLoggedToBackend(request):
                    null == this.userWebAPI.getLoggedInFrontendUser(request);

            Logger.debug(this, "The user is in backend: " + isBackend +
                            ", is not logged: " + isNotLogged);
        } catch (PortalException | SystemException e) {

            Logger.error(this, e.getMessage(), e);
            isNotLogged = true;
        }

        return isNotLogged;
    } // isNotLogged.

    private void printMetaData(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final Configuration configuration) throws ServletException {

        // First, get the Entity descriptor.
        final EntityDescriptor descriptor =
                configuration.getMetaDescriptorService()
                        .getServiceProviderEntityDescriptor(configuration);
        Writer writer = null;

        try {

            // get ready to convert it to XML.
            response.setContentType(TEXT_XML);
            writer = response.getWriter();
            this.metaDataXMLPrinter.print(descriptor, writer);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParserConfigurationException | TransformerException | IOException | MarshallingException e) {

            Logger.error(this.getClass(),
                    e.getMessage(), e);
            throw new ServletException(e);
        } finally {

            IOUtils.closeQuietly(writer);
        }
    } // printMetaData.

    private boolean autoLogin (final HttpServletRequest   request,
                            final HttpServletResponse    response,
                            final HttpSession             session,
                            final Configuration     configuration) throws IOException {

        final User user =
                this.samlAuthenticationService.getUser
                        (request, response, session, configuration.getSiteName());
        boolean continueFilter = true; // by default continue with the filter

        if (null != user) {

            // we are going to do the autologin, so if the session is null, create it!
            try {

                Logger.info(this, "User returned by SAML Service, id " + user.getUserId() +
                        ", user Map: " + user.toMap());
            } catch (Exception e) {

                Logger.error(this, e.getMessage(), e);
            }

            // todo: 3.7 this should be changed to LoginService
            final boolean doCookieLogin = LoginFactory.doCookieLogin(PublicEncryptionFactory.encryptString
                    (user.getUserId()), request, response);

            Logger.info(this, "Login result by LoginFactory: " + doCookieLogin);

            if (doCookieLogin) {

                if (null != session && null != user.getUserId()) {
                    // this is what the PortalRequestProcessor needs to check the login.
                    Logger.info(this, "Setting the user id on the session: " + user.getUserId());

                    final String uri = session.getAttribute(ORIGINAL_REQUEST) != null ? (String)session.getAttribute(ORIGINAL_REQUEST) : request.getRequestURI();
                    session.removeAttribute(ORIGINAL_REQUEST);

                    if(this.isBackEndAdmin(session, uri)) {

                        session.setAttribute(com.liferay.portal.util.WebKeys.USER_ID, user.getUserId());
                        PrincipalThreadLocal.setName(user.getUserId());
                    }
                    // depending if it is a redirection or not, continue.
                    continueFilter = this.checkRedirection(request, response, session);

                    this.doAuthenticationLoginSecurityLog(request, configuration, user);
                }
            }
        } else {

            // if it was a saml request and could not get the user, throw an error
            if (this.samlAuthenticationService.isValidSamlRequest
                    (request, response, configuration.getSiteName())) {

                Logger.error(this, "This request is a saml request, but couldn't resolve the user so throwing an internal error");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                continueFilter = false; // not continue. since it is an error redirect.
            }
        }

        return continueFilter;
    } // autoLogin.



    private boolean isBackEndAdmin(final HttpSession session, final String uri) {

        return (session.getAttribute(com.dotmarketing.util.WebKeys.ADMIN_MODE_SESSION) != null) ||
                // todo: on higher versions this hack should be checked for a better criteria
                this.isBackEndLoginPage(uri);
    } // isBackEndAdmin.

    private boolean isBackEndLoginPage(final String uri) {

        return  uri.startsWith("/html/portal/login")        ||
                uri.startsWith("/c/public/login")           ||
                uri.startsWith("/c/portal_public/login")    ||
                uri.startsWith("/c/portal/logout");
    }

    private boolean isFrontEndLoginPage (final String uri) {

        return uri.startsWith("/dotCMS/login") ||
            uri.startsWith("/application/login");
    }

    private boolean checkRedirection(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final HttpSession session) {

        final String redirectAfterLogin = (String) session.
                getAttribute(WebKeys.REDIRECT_AFTER_LOGIN);

        if (null != redirectAfterLogin) {

            session.removeAttribute(WebKeys.REDIRECT_AFTER_LOGIN);
            final String currentRequest = request.getRequestURI() +
                    ((null != request.getQueryString())? "?" + request.getQueryString():
                            StringUtils.EMPTY);
            // if it is not the same request.
            if (!currentRequest.equals(redirectAfterLogin)) {

                try {

                    if (this.isBackEndLoginPage(redirectAfterLogin)
                            || this.isFrontEndLoginPage(redirectAfterLogin)) {

                        if (this.isBackEndAdmin(session, redirectAfterLogin)) {

                            Logger.info(this, "Redirecting to: /c");
                            response.sendRedirect("/c");
                        } else { // if it is front end

                            Logger.info(this, "Redirecting to: /");
                            response.sendRedirect("/");
                        }
                    } else {

                        Logger.info(this, "Redirecting to: " + redirectAfterLogin);
                        response.sendRedirect(redirectAfterLogin);
                    }
                    return false; // not continue. since it is a redirect.
                } catch (IOException e) {

                    Logger.error(this, e.getMessage(), e);
                }
            }
        }

        return true; // continue with the current request.
    } // checkRedirection.

    @Override
    public void destroy() {

    }
} // E:O:F:SamlAccessFilter.
