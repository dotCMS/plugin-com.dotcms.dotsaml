package com.dotcms.plugin.saml.v3.filter;

import com.dotcms.plugin.saml.v3.OpenSamlAuthenticationServiceImpl;
import com.dotcms.plugin.saml.v3.SamlAuthenticationService;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.cms.login.factories.LoginFactory;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.WebKeys;
import com.liferay.portal.model.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Access filter for SAML plugin, it does the autologin and also redirect to the
 * IDP if the user is not logged in.
 * @author jsanca
 */
public class SamlAccessFilter implements Filter {

    private final SamlAuthenticationService samlAuthenticationService;

    public SamlAccessFilter() {

        this(new OpenSamlAuthenticationServiceImpl());
    }

    @VisibleForTesting
    public SamlAccessFilter(final SamlAuthenticationService samlAuthenticationService) {

        this.samlAuthenticationService = samlAuthenticationService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {

        final HttpServletResponse response = (HttpServletResponse) res;
        final HttpServletRequest  request  = (HttpServletRequest) req;
        final HttpSession         session  = request.getSession(false);

        this.autoLogin(request, response, session);

        if (null == session || null == session.getAttribute(WebKeys.CMS_USER)) {

                Logger.warn(this.getClass(),
                        "Doing Saml Login Redirection when request: " +
                                request.getRequestURI() + "?" + request.getQueryString());

                //if we don't have a redirect yet
                session.setAttribute(WebKeys.REDIRECT_AFTER_LOGIN,
                        request.getRequestURI() + '?' + request.getQueryString());

                this.samlAuthenticationService.authentication(request, response);
                return;
        }

        chain.doFilter(request, response);
    }

    private void autoLogin (final HttpServletRequest request,
                            final HttpServletResponse response,
                            final HttpSession         session) {

        final User user =
                this.samlAuthenticationService.getUser(request, response);

        if (null != session && null != user) {

            LoginFactory.doCookieLogin(PublicEncryptionFactory.encryptString
                    (user.getUserId()), request, response);
        }
    } // autoLogin.

    @Override
    public void destroy() {

    }
} // E:O:F:SamlAccessFilter.
