package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.SiteConfigurationResolver;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationService;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This service is in charge of create/refresh the configuration and set the services needed to get the
 * saml configuration.
 * @author jsanca
 */
public class SiteCofigurationInitializerService implements Initializer {

    private final AtomicBoolean isDone = new AtomicBoolean(false);
    private final SiteConfigurationParser siteConfigurationParser
            = new SiteConfigurationParser();

    @Override
    public void init(final Map<String, Object> context) {

        this.isDone.set(false);

        final SiteConfigurationService siteConfigurationService;
        final Map<String, Configuration> configurationMap;

        final SiteConfigurationResolver siteConfigurationResolver =
                new SiteConfigurationResolver();

        try {

            Logger.debug(this, "Parsing SAML configuration");
            configurationMap =
                    this.siteConfigurationParser.getConfiguration();

        } catch (IOException | DotDataException | DotSecurityException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        Logger.debug(this, "SAML configuration, map = " + configurationMap);
        siteConfigurationService = new SiteConfigurationService(configurationMap);

        InstancePool.put(SiteConfigurationService.class.getName(), siteConfigurationService);
        InstancePool.put(SiteConfigurationResolver.class.getName(), siteConfigurationResolver);

        this.isDone.set(true);
    }

    @Override
    public boolean isInitializationDone() {
        return this.isDone.get();
    }
} // E:O:F:SiteCofigurationInitializerService.
