package com.dotcms.plugin.saml.v3;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 *  Provides Open SAML Authentication Service.
 *  Most of the configuration comes from the dotmarketing-config.properties
 * @author jsanca
 */
public interface SamlAuthenticationService extends Serializable {

    /**
     * Authentication with SAML
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    void authentication(final HttpServletRequest request, final HttpServletResponse response);

} // E:O:F:SamlAuthenticationService.
