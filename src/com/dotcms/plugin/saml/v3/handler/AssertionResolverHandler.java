package com.dotcms.plugin.saml.v3.handler;

import com.dotcms.plugin.saml.v3.config.IdpConfig;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.json.JSONException;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml.saml2.core.Assertion;

/**
 * This handler is in charge of resolve the user based on a saml callback
 * 
 * @author jsanca
 */
public interface AssertionResolverHandler extends Serializable
{
	public static final int DOT_SAML_CLOCK_SKEW_DEFAULT_VALUE = 1000;
	public static final int DOT_SAML_MESSAGE_LIFE_DEFAULT_VALUE = 2000;

	/**
	 * Returns true if it is a valid saml request.
	 *
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @return boolean
	 */
	boolean isValidSamlRequest( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig );

	/**
	 * Resolve the user based on a SAML callback, depending on the
	 * implementation the criteria to check if it is a saml request and how to
	 * handle might be different.
	 *
	 * @param request  {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param idpConfig {@link IdpConfig}
	 * @return User
	 */
	Assertion resolveAssertion( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws DotDataException, IOException, JSONException;

}
