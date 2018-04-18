package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.config.IdpConfig;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.json.JSONException;

import com.liferay.portal.model.User;

import java.io.IOException;
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
	/**
	 * Determine if the request is a valid saml request depending on the
	 * siteName configuration
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @return boolean
	 */
	public boolean isValidSamlRequest( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig );

	/**
	 * Authentication with SAML
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws DotDataException 
	 */
	void authentication( final HttpServletRequest request, final HttpServletResponse response ) throws DotDataException, IOException, JSONException;
	void authentication( final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final IdpConfig idpConfig ) throws DotDataException, IOException;

	/**
	 * Do the logout call for SAML
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws DotDataException 
	 */
	void logout( final HttpServletRequest request, final HttpServletResponse response, final NameID nameID, final String sessionIndexValue, final IdpConfig idpConfig ) throws DotDataException, IOException, JSONException;

	/**
	 * Pre: the request parameter SAML_ART_PARAM_KEY must exists 
	 * Resolve the assertion by making a call to the idp.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @return Assertion
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws DotDataException 
	 */
	Assertion resolveAssertion( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws DotDataException, IOException, JSONException;

	/**
	 * Perform the logic to get or create the user from the SAML and DotCMS If
	 * the SAML_ART_PARAM_KEY, will resolve the Assertion by calling a Resolver
	 * and will create/get/update the user on the dotcms data.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @return User
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	User getUser( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws DotDataException, JSONException, IOException;

	/**
	 * Perform the logic to get or create the user from the SAML and DotCMS If
	 * the SAML_ART_PARAM_KEY, will resolve the Assertion by calling a Resolver
	 * and will create/get/update the user on the dotcms data.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param loginHttpSession {@link HttpSession} session to store the
	 * @param idpConfig {@link IdpConfig}
	 * @return User
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	User getUser( final HttpServletRequest request, final HttpServletResponse response, final HttpSession loginHttpSession, final IdpConfig idpConfig ) throws DotDataException, JSONException, IOException;

}
