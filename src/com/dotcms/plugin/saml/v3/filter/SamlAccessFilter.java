package com.dotcms.plugin.saml.v3.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dotcms.cms.login.LoginServiceAPI;
import com.dotcms.plugin.saml.v3.config.EndpointHelper;
import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotcms.plugin.saml.v3.service.OpenSamlAuthenticationServiceImpl;
import com.dotcms.plugin.saml.v3.service.SamlAuthenticationService;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;
import com.dotcms.plugin.saml.v3.util.MetaDataXMLPrinter;
import com.dotcms.plugin.saml.v3.util.SiteIdpConfigResolver;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.business.web.HostWebAPI;
import com.dotmarketing.business.web.LanguageWebAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.WebKeys;
import com.dotmarketing.util.json.JSONException;

/**
 * Access filter for SAML plugin, it does the autologin and also redirect to the
 * IDP if the user is not logged in.
 * 
 * @author jsanca
 */
// todo: not migrated
public class SamlAccessFilter extends SamlFilter implements Filter {
	public SamlAccessFilter() {
		super(InstanceUtil.newInstance(
				Config.getStringProperty(DotSamlConstants.DOT_SAML_AUTHENTICATION_SERVICE_CLASS_NAME, null),
				OpenSamlAuthenticationServiceImpl.class));
	}

	@VisibleForTesting
	public SamlAccessFilter(final SamlAuthenticationService samlAuthenticationService) {
		super(samlAuthenticationService, new MetaDataXMLPrinter(), WebAPILocator.getHostWebAPI(),
				WebAPILocator.getLanguageWebAPI(), APILocator.getPermissionAPI(), APILocator.getIdentifierAPI(),
				APILocator.getContentletAPI(), WebAPILocator.getUserWebAPI(), APILocator.getLoginServiceAPI());
	}

	@VisibleForTesting
	public SamlAccessFilter(final SamlAuthenticationService samlAuthenticationService,
			final MetaDataXMLPrinter metaDataXMLPrinter, final HostWebAPI hostWebAPI,
			final LanguageWebAPI languageWebAPI, final PermissionAPI permissionAPI, final IdentifierAPI identifierAPI,
			final ContentletAPI contentletAPI, final UserWebAPI userWebAPI, final LoginServiceAPI loginService) {
		super(samlAuthenticationService, metaDataXMLPrinter, hostWebAPI, languageWebAPI, permissionAPI, identifierAPI,
				contentletAPI, userWebAPI, loginService);
	}

	private HttpSession getSession (final HttpServletRequest httpServletRequest) {

		HttpSession session = httpServletRequest.getSession(false);
		return session != null? session: httpServletRequest.getSession(true);
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
			final FilterChain chain) throws IOException, ServletException {
		final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
		final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		HttpSession session = this.getSession(httpServletRequest);
		String redirectAfterLogin = null;
		boolean isLogoutNeed = false;

		if (super.isByPass(httpServletRequest, session)) {
			Logger.debug(this, "Using SAML by pass");
			chain.doFilter(httpServletRequest, httpServletResponse);
			return;
		}

		try {
			final IdpConfig idpConfig = SiteIdpConfigResolver.getInstance().resolveIdpConfig(httpServletRequest);

			// If idpConfig is null, means this site does not need SAML
			// processing
			if (idpConfig != null && idpConfig.isEnabled()) {
				isLogoutNeed = DotsamlPropertiesService.getOptionBoolean(idpConfig,
						DotsamlPropertyName.DOTCMS_SAML_IS_LOGOUT_NEED);

				// check if there is any exception filter path, to avoid to
				// canApply all the logic.
				if (!super.checkAccessFilters(httpServletRequest.getRequestURI(),
						EndpointHelper.getAccessFilterArray(idpConfig))
						&& super.checkIncludePath(httpServletRequest.getRequestURI(),
								EndpointHelper.getIncludePathArray(idpConfig), httpServletRequest)) {
					// if it is an url to canApply the Saml access logic,
					// determine if the autoLogin is possible
					// the autologin will works if the SAMLArt (Saml artifact
					// id) is in the request query string
					// for artifact resolution or SAMLResponse for post
					// resolution.
					
					final AutoLoginResult autoLoginResult = super.autoLogin(httpServletRequest, httpServletResponse,
							session, idpConfig);

					// we have to assign again the session, since the
					// doAutoLogin might be renewed.
					session = autoLoginResult.getSession();

					// if the auto login couldn't logged the user, then send it
					// to the IdP login page (if it is not already logged in).
					if (null == session || super.isNotLogged(httpServletRequest, session)) {
						Logger.debug(this, "There's no logged-in user. Processing SAML request...");
						super.doRequestLoginSecurityLog(httpServletRequest, idpConfig);

						final String originalRequest = httpServletRequest.getRequestURI()
								+ ((null != httpServletRequest.getQueryString())
										? "?" + httpServletRequest.getQueryString() : StringUtils.EMPTY);

						redirectAfterLogin = (UtilMethods
								.isSet(httpServletRequest.getParameter(REFERRER_PARAMETER_KEY)))
										? httpServletRequest.getParameter(REFERRER_PARAMETER_KEY) :
										// this is safe, just to make a
										// redirection when the user get's
										// logged.
										originalRequest;

						Logger.debug(this.getClass(),
								"Executing SAML Login Redirection with request: " + redirectAfterLogin);

						// if we don't have a redirect yet
						if (null != session) {
							session.setAttribute(WebKeys.REDIRECT_AFTER_LOGIN, redirectAfterLogin);
							session.setAttribute(ORIGINAL_REQUEST, originalRequest);
						}

						try {
							// this will redirect the user to the IdP Login
							// Page.
							super.samlAuthenticationService.authentication(httpServletRequest, httpServletResponse);
						} catch (DotSamlException | DotDataException exception) {
							Logger.error(this, "An error occurred when redirecting to the IdP Login page: " +
									exception.getMessage(), exception);
							Logger.debug(this, "An error occurred when redirecting to the IdP Login page. Setting 500 " +
									"response status.");
							httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						}

						return;
					}
				}

				// Starting the logout
				// if it is logout
                Logger.debug(this, "----------------------------- doFilter --------------------------------");
				Logger.debug(this, "- isLogoutNeed = " + isLogoutNeed);
                Logger.debug(this, "- httpServletRequest.getRequestURI() = " + httpServletRequest.getRequestURI());
				if (isLogoutNeed && session != null && super.isLogoutRequest(httpServletRequest.getRequestURI(),
						EndpointHelper.getLogoutPathArray(idpConfig))) {
					if (super.doLogout(httpServletResponse, httpServletRequest, session, idpConfig)) {
						return;
					}
				}

			} else {
				Logger.info(this, "No idpConfig for site '" + httpServletRequest.getServerName()
						+ "'. No SAML filtering for this request: " + httpServletRequest.getRequestURI());
			}

		} catch (final JSONException | DotDataException exception) {
			Logger.debug(this, "Error [" + exception.getMessage() + "] Unable to get idpConfig for Site '" +
					httpServletRequest.getServerName() + "'. Incoming URL: " + httpServletRequest.getRequestURL());
		}

		chain.doFilter(httpServletRequest, httpServletResponse);
	}

	@Override
	public void destroy() {

	}

}
