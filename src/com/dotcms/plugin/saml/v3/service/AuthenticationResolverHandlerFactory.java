package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import org.apache.velocity.app.VelocityEngine;

import java.io.Serializable;

/**
 * A factory for the {@link AuthenticationHandler}
 * 
 * @author jsanca
 */
public class AuthenticationResolverHandlerFactory implements Serializable {

	private final VelocityEngine           velocityEngine;


	public AuthenticationResolverHandlerFactory(final VelocityEngine           velocityEngine) {

		this.velocityEngine           = velocityEngine;
	}

	/**
	 * Get the authentication handler depending on the configuration.
	 *
	 * @param idpConfig
	 *            {@link IdpConfig}
	 * @return AuthenticationHandler
	 */
	public AuthenticationHandler getAuthenticationHandlerForSite(final IdpConfig idpConfig) {

		final String authenticationProtocolBinding = DotsamlPropertiesService.getOptionString(idpConfig,
				DotsamlPropertyName.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING,
				DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_REDIRECT);

		switch (authenticationProtocolBinding) {

			case DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_REDIRECT:
				return new HttpRedirectAuthenticationHandler();
			case DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_POST:
				return new HttpPOSTAuthenticationHandler(this.velocityEngine);
		}

		return new HttpRedirectAuthenticationHandler();
	}
}
