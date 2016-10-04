package com.dotcms.plugin.saml.v3.config;

import com.dotmarketing.util.Logger;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the configuration per site.
 * @author jsanca
 */
public class SiteConfigurationService implements Serializable {

    private final Map<String, Configuration> configurationBySiteMap;
    private final Configuration siteDefaultConfiguration;
    private final String        siteNameDefaultConfiguration;

    public SiteConfigurationService(final Map<String, Configuration> configurationBySiteMap) {


        Configuration defaultConfiguration = null;
        String        siteNameDefaultConfiguration = null;
        this.configurationBySiteMap = Collections.
                unmodifiableMap(configurationBySiteMap);

        Logger.debug(this, "Processing the site configuration, with the value: " + configurationBySiteMap);

        for (Map.Entry<String, Configuration> configuration : this.configurationBySiteMap.entrySet()) {

            if (configuration.getValue().isDefault()) {

                defaultConfiguration            = configuration.getValue();
                siteNameDefaultConfiguration    = configuration.getKey();

                Logger.debug(this, "Using as a default site: " + defaultConfiguration);
            }
        }

        this.siteDefaultConfiguration     = defaultConfiguration;
        this.siteNameDefaultConfiguration = siteNameDefaultConfiguration;
    } // SiteConfigurationService.

    /**
     * Get the name of the default site
     * @return String
     */
    public String getDefaultSiteName () {

        return this.siteNameDefaultConfiguration;
    }

    /**
     * Get the default site configuration
     * @return Configuration
     */
    public Configuration getDefaultSiteConfiguration () {

        return this.siteDefaultConfiguration;
    }

    /**
     * Get the site names
     * @return Set
     */
    public Set<String>  getSiteNames () {

        return this.configurationBySiteMap.keySet();
    } // getSiteNames.

    /**
     * Get Configuration by site
     * @param site {@link String}
     * @return Configuration
     */
    public Configuration getConfigurationBySite (final String site) {

        Logger.debug(this, ((this.configurationBySiteMap.containsKey(site))?
                        "Found a configuration for the site: " + site:
                        "Could not find a configuration for the site: " + site
                                + ", using the default site: " + this.getDefaultSiteName()
                    ));

        return  (this.configurationBySiteMap.containsKey(site))?
                this.configurationBySiteMap.get(site):
                this.siteDefaultConfiguration;

    } // getConfigurationBySite.
} // E:O:F:SiteConfigurationService.
