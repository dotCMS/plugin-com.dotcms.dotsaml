package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import org.apache.velocity.app.VelocityEngine;

import java.io.Serializable;

/**
 * A factory for the {@link LogoutHandler}
 * 
 * @author jsanca
 */
public class LogoutResolverHandlerFactory implements Serializable {

	private final VelocityEngine           velocityEngine;


	public LogoutResolverHandlerFactory(final VelocityEngine           velocityEngine) {

		this.velocityEngine           = velocityEngine;
	}

	/**
	 * Get the Logout Handler depending on the configuration
	 *
	 * @param idpConfig
	 *            {@link IdpConfig}
	 * @return LogoutHandler
	 */
	public LogoutHandler getLogoutHandlerForSite(final IdpConfig idpConfig) {

		final String logoutProtocolBinding = DotsamlPropertiesService.getOptionString(idpConfig,
				DotsamlPropertyName.DOT_SAML_LOGOUT_PROTOCOL_BINDING,
				DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_REDIRECT);

		switch (logoutProtocolBinding) {

			case DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_REDIRECT:
				return new HttpRedirectLogoutHandler();
			case DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_POST:
				return new HttpPOSTLogoutHandler(this.velocityEngine);
			case "Http-Okta":
				return new HttpOktaLogoutHandler();
		}

		return new HttpRedirectLogoutHandler();
	}
}
