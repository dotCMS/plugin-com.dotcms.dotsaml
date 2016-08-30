package com.dotcms.plugin.saml.v3;

import com.liferay.portal.model.User;
import org.opensaml.saml.saml2.core.Assertion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 *  Provides Open SAML Authentication Service.
 *  Most of the configuration comes from the dotmarketing-config.properties
 * @author jsanca
 */
public interface SamlAuthenticationService extends Serializable {

    public static final String SAML_ART_PARAM_KEY = "SAMLart";

    /**
     * Authentication with SAML
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    void authentication(final HttpServletRequest request, final HttpServletResponse response);

    /**
     * Pre: the request parameter SAML_ART_PARAM_KEY must exists
     * Resolve the assertion by making a soap call to the idp.
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return Assertion
     */
    Assertion resolveAssertion(final HttpServletRequest request,
                                         final HttpServletResponse response);
    /**
     * Perform the logic to get or create the user from the SAML and DotCMS
     * If the SAML_ART_PARAM_KEY, will resolve the Assertion by calling a Soap
     * and will create/get/update the user on the dotcms data.
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return User
     */
    User getUser(final HttpServletRequest request, final HttpServletResponse response);
} // E:O:F:SamlAuthenticationService.
