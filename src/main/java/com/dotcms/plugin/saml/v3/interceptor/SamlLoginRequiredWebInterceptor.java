package com.dotcms.plugin.saml.v3.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotcms.plugin.saml.v3.OpenSamlAuthenticationServiceImpl;
import com.dotcms.plugin.saml.v3.SamlAuthenticationService;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.WebKeys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Saml Login Interceptor.
 * If the user is not logged in, will redirect to the IdP.
 *
 * @author jsanca
 */
public class SamlLoginRequiredWebInterceptor implements WebInterceptor {

    private final SamlAuthenticationService samlAuthenticationService;

    public SamlLoginRequiredWebInterceptor() {

        this(new OpenSamlAuthenticationServiceImpl());
    }

    @VisibleForTesting
    public SamlLoginRequiredWebInterceptor(
            final SamlAuthenticationService samlAuthenticationService) {

        this.samlAuthenticationService = samlAuthenticationService;
    }

    @Override
    public Result intercept(final HttpServletRequest request,
                            final HttpServletResponse response) throws IOException {

        final HttpSession session = request.getSession(false);
        Result result = Result.NEXT;

        // if we are not logged in, go to login page
        if (null == session.getAttribute(WebKeys.CMS_USER)) {

            Logger.warn(this.getClass(),
                    "Doing Saml Login Required for RequestURI: " +
                            request.getRequestURI() + "?" + request.getQueryString());

            //if we don't have a redirect yet
            session.setAttribute(WebKeys.REDIRECT_AFTER_LOGIN,
                    request.getRequestURI() + "?" + request.getQueryString());

            this.samlAuthenticationService.authentication(request, response);
            result = Result.SKIP_NO_CHAIN; // needs to stop the filter chain.
        }

        return result; // if it is log in, continue!
    } // intercept.
} // SamlLoginRequiredWebInterceptor.
