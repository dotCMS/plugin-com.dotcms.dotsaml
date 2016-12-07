package com.dotcms.plugin.saml.v3.handler;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.SiteConfigurationResolver;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.util.InstancePool;

import java.io.Serializable;

/**
 * A factory for the {@link AssertionResolverHandler}
 * @author jsanca
 */
public class AssertionResolverHandlerFactory implements Serializable {

    /**
     * Get the resolver assertion depending on the site.
     * @param siteName
     * @return
     */
    public AssertionResolverHandler getAssertionResolverForSite(final String siteName) {

        final SiteConfigurationResolver resolver      = (SiteConfigurationResolver) InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration             configuration = resolver.resolveConfiguration(siteName);
        final String                    className     = configuration.getStringProperty
                (DotSamlConstants.DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME, null);

        final AssertionResolverHandler  assertionResolverHandler =
                (!UtilMethods.isSet(className))?
                    this.getDefaultAssertionResolverHandler():
                    this.getAssertionResolverHandler(className);

        Logger.debug(this, "Getting the assertion resolver for the site: " + siteName +
                        ", with the class: " + assertionResolverHandler);

        return assertionResolverHandler;
    } // getAssertionResolverForSite.

    private AssertionResolverHandler getDefaultAssertionResolverHandler() {

        return this.getAssertionResolverHandler(SOAPArtifactAssertionResolverHandlerImpl.class.getName());
    }

    private AssertionResolverHandler getAssertionResolverHandler(final String className) {

        return (AssertionResolverHandler) InstancePool.get(className);
    } // getAssertionResolverHandler.

} // E:O:F:AssertionResolverHandlerFactory.
