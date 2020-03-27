package com.dotcms.plugin.saml.v3.handler;

import java.io.Serializable;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import com.liferay.util.InstancePool;

/**
 * A factory for the {@link AssertionResolverHandler}
 * 
 * @author jsanca
 */

public class AssertionResolverHandlerFactory implements Serializable {
	private static final long serialVersionUID = 2434118681822205248L;

	/**
	 * Get the resolver assertion depending on the site.
	 *
	 * @param idpConfig
	 *            {@link IdpConfig}
	 * @return
	 */
	public AssertionResolverHandler getAssertionResolverForSite(final IdpConfig idpConfig) {
		String className = null;

		try {
			className = DotsamlPropertiesService.getOptionString(idpConfig,
					DotsamlPropertyName.DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME);
		} catch (Exception exception) {
			Logger.info(this,
					"Optional property not set: "
							+ DotsamlPropertyName.DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME.getPropertyName()
							+ " for idpConfig: " + idpConfig.getId() + " Using default.");
		}

		final AssertionResolverHandler assertionResolverHandler = (!UtilMethods.isSet(className))
				? this.getDefaultAssertionResolverHandler() : this.getAssertionResolverHandler(className);

		Logger.debug(this, "Getting the assertion resolver for the idpConfig: " + idpConfig.getId()
				+ ", with the class: " + assertionResolverHandler);

		return assertionResolverHandler;
	}

	private AssertionResolverHandler getDefaultAssertionResolverHandler() {
		return this.getAssertionResolverHandler(HttpPostAssertionResolverHandlerImpl.class.getName());
	}

	private AssertionResolverHandler getAssertionResolverHandler(final String className) {
		return (AssertionResolverHandler) InstancePool.get(className);
	}
}
