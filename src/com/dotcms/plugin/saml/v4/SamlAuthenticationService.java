package com.dotcms.plugin.saml.v4;

import com.liferay.portal.model.User;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;

/**
 * Provides Open SAML Authentication Service. Most of the configuration comes
 * from the dotmarketing-config.properties
 * 
 * @author jsanca
 */
public interface SamlAuthenticationService extends Serializable
{
	public static final String SAML_NAME_ID_SESSION_ATTR = "SAML_NAME_ID";
	public static final String SAML_ART_PARAM_KEY = "SAMLart";

	/**
	 * Determine if the request is a valid saml request depending on the
	 * siteName configuration
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param siteName {@link String}
	 * @return boolean
	 */
	public boolean isValidSamlRequest( final HttpServletRequest request, final HttpServletResponse response, final String siteName );

	/**
	 * Authentication with SAML
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param siteName {@link String}
	 */
	void authentication( final HttpServletRequest request, final HttpServletResponse response, final String siteName );

	/**
	 * Do the logout call for SAML
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param siteName {@link String}
	 */
	void logout( final HttpServletRequest request, final HttpServletResponse response, final NameID nameID, final String sessionIndexValue, final String siteName );

	/**
	 * Pre: the request parameter SAML_ART_PARAM_KEY must exists 
	 * Resolve the assertion by making a call to the idp.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param siteName {@link String}
	 * @return Assertion
	 */
	Assertion resolveAssertion( final HttpServletRequest request, final HttpServletResponse response, final String siteName );

	/**
	 * Perform the logic to get or create the user from the SAML and DotCMS If
	 * the SAML_ART_PARAM_KEY, will resolve the Assertion by calling a Resolver
	 * and will create/get/update the user on the dotcms data.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param siteName {@link String}
	 * @return User
	 */
	User getUser( final HttpServletRequest request, final HttpServletResponse response, final String siteName );

	/**
	 * Perform the logic to get or create the user from the SAML and DotCMS If
	 * the SAML_ART_PARAM_KEY, will resolve the Assertion by calling a Resolver
	 * and will create/get/update the user on the dotcms data.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param loginHttpSession {@link HttpSession} session to store the
	 * @param siteName {@link String}
	 * @return User
	 */
	User getUser( final HttpServletRequest request, final HttpServletResponse response, final HttpSession loginHttpSession, final String siteName );
}
