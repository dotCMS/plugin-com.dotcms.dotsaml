package com.dotcms.plugin.saml.v3.interceptor;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotcms.plugin.saml.v3.OpenSamlAuthenticationServiceImpl;
import com.dotcms.plugin.saml.v3.SamlAuthenticationService;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.cms.login.factories.LoginFactory;
import com.liferay.portal.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * If the SAMLart is part of the request, the SAML Auto Login might happen.
 * @author jsanca
 */
public class SamlAutoLoginWebInterceptor implements WebInterceptor {

    private final SamlAuthenticationService samlAuthenticationService;

    public SamlAutoLoginWebInterceptor() {

        this(new OpenSamlAuthenticationServiceImpl());
    }

    @VisibleForTesting
    public SamlAutoLoginWebInterceptor(final SamlAuthenticationService samlAuthenticationService) {

        this.samlAuthenticationService = samlAuthenticationService;
    }

    @Override
    public Result intercept(final HttpServletRequest request,
                            final HttpServletResponse response) throws IOException {

        Result result = Result.NEXT;
        final HttpSession session  = request.getSession(false);

        final User user =
                this.samlAuthenticationService.getUser(request, response);

        if (null != user && null != session) {

            if (LoginFactory.doCookieLogin(PublicEncryptionFactory.encryptString
                    (user.getUserId()), request, response)) {

                result = Result.SKIP;
            }
        }

        return result;
    } // intercept.

} // E:O:F:SamlAutoLoginWebInterceptor.
