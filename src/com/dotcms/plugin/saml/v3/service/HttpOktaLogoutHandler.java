package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotmarketing.util.Logger;
import org.opensaml.saml.saml2.core.NameID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implements the Logout handler by POST
 * @author jsanca
 */
public class HttpOktaLogoutHandler implements LogoutHandler {


    @Override
    public void handle(final HttpServletRequest  request,
                       final HttpServletResponse response,
                       final NameID nameID,
                       final String sessionIndexValue,
                       final IdpConfig idpConfig) {

        Logger.info(this.getClass().getName(), "Processing saml logout Okta for nameID: " + nameID);

        final String logoutCallback = DotsamlPropertiesService.getOptionString(idpConfig,
                DotsamlPropertyName.DOT_SAML_LOGOUT_SERVICE_ENDPOINT_URL,
                "/dotAdmin/show-logout");

        final String logoutPath = DotsamlPropertiesService.getOptionString(idpConfig,
                DotsamlPropertyName.DOT_SAML_LOGOUT_OKTA_URL);

        try {

            final String redirectUrl = logoutPath + "?fromURI=" + logoutCallback;
            Logger.info(this.getClass().getName(), "Logout redirect: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {

            Logger.error(this.getClass().getName(), e.getMessage(), e);
        }
    }
}
