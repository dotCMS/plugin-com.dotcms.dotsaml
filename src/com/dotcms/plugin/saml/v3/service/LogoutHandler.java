package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import org.opensaml.saml.saml2.core.NameID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Encapsulates the Logout Handler, could be POST or Redirect (default)
 * @author jsanca
 */
public interface LogoutHandler {

    /**
     * Handles the logout method
     * @param request    HttpServletRequest
     * @param response  {@link HttpServletResponse}
     * @param nameID    {@link NameID}
     * @param sessionIndexValue {@link String}
     * @param idpConfig {@link IdpConfig}
     */
    void handle(final HttpServletRequest  request,
                final HttpServletResponse response,
                final NameID nameID,
                final String sessionIndexValue,
                final IdpConfig idpConfig);
}
