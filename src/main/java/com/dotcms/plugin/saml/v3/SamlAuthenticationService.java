package com.dotcms.plugin.saml.v3;

import com.liferay.portal.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 *  Provides Open SAML Authentication Service.
 *  Most of the configuration comes from the dotmarketing-config.properties
 * @author jsanca
 */
public interface SamlAuthenticationService extends Serializable {

    public static final String SAM_LART_PARAM_KEY = "SAMLart";

    /**
     * Authentication with SAML
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    void authentication(final HttpServletRequest request, final HttpServletResponse response);

    /**
     * Perform the logic to get or create the user from the SAML and DotCMS
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return User
     */
    User getUser(final HttpServletRequest request, final HttpServletResponse response);
} // E:O:F:SamlAuthenticationService.
