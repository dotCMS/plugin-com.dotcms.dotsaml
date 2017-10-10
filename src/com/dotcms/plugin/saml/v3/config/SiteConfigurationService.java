package com.dotcms.plugin.saml.v3.config;

import com.dotmarketing.util.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates the configuration per site.
 * @author jsanca
 */
public class SiteConfigurationService implements Serializable {

    private final Map<String, Configuration> configurationBySiteMap;

    public SiteConfigurationService(final Map<String, Configuration> configurationBySiteMap) {

        this.configurationBySiteMap = new ConcurrentHashMap<>(configurationBySiteMap);

    } // SiteConfigurationService.

    /**
     * Updates a set of configurations.
     * @param configurationBySiteMap Map
     */
    public void updateConfigurations (final Map<String, Configuration> configurationBySiteMap) {
        this.configurationBySiteMap.putAll(configurationBySiteMap);
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
                    ));

        return this.configurationBySiteMap.get(site);

    } // getConfigurationBySite.

    public void setConfigurationBySite (final String site, final Configuration conf){
        this.configurationBySiteMap.remove(site);

        if (conf != null) {
            this.configurationBySiteMap.put(site, conf);
        }
    }
} // E:O:F:SiteConfigurationService.
