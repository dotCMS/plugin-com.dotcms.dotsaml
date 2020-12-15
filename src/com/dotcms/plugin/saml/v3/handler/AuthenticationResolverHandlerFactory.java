package com.dotcms.plugin.saml.v3.handler;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.util.InstancePool;

import java.io.Serializable;

/**
 * A factory for the {@link AuthenticationHandler}
 * 
 * @author jsanca
 */
// migrated
public class AuthenticationResolverHandlerFactory implements Serializable {
	private static final long serialVersionUID = 2434118681822205248L;

	/**
	 * Get the resolver assertion depending on the site.
	 *
	 * @param idpConfig
	 *            {@link IdpConfig}
	 * @return
	 */
	public AuthenticationHandler getAuthenticationHandlerForSite(final IdpConfig idpConfig) {

		final String authenticationProtocolBinding = DotsamlPropertiesService.getOptionString(idpConfig, DotsamlPropertyName.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING,
				DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_REDIRECT);

		switch (authenticationProtocolBinding) {

			case DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_REDIRECT:
				return new HttpRedirectAuthenticationHandler();
			case DotSamlConstants.DOTCMS_SAML_AUTHN_PROTOCOL_BINDING_POST:
				return new HttpPOSTAuthenticationHandler();
		}

		return new HttpRedirectAuthenticationHandler();
	}
}
