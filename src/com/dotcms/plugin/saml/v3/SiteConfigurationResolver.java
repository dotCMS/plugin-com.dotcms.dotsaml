package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationService;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Resolve the Site configuration for
 * @author jsanca
 */
public class SiteConfigurationResolver implements Serializable {

    /**
     * Returns the Configuration associate to the current site, if it is not found a match
     * returns the default one site config.
     * @param request {@link HttpServletRequest}
     * @return Configuration
     */
    public Configuration resolveConfiguration (final HttpServletRequest request) {

        final String serverName = request.getServerName();
        final SiteConfigurationService siteConfigurationService
                = (SiteConfigurationService) InstancePool.get(SiteConfigurationService.class.getName());
        final Configuration configuration = siteConfigurationService.getConfigurationBySite(serverName);

        Logger.debug(this, "Resolving the configuration: " + configuration +
                             ", for the site: " + serverName + ", sites availables:" +
                             siteConfigurationService.getSiteNames());

        return configuration;
    }

} // E:O:F:SiteConfigurationResolver
