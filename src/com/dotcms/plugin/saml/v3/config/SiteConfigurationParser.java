package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.util.Config;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Take a host field and creates a SiteConfigurationBean
 *
 * @author jsanca
 */
public class SiteConfigurationParser implements Serializable {

    private HostAPI hostAPI = APILocator.getHostAPI();
    private UserAPI userAPI = APILocator.getUserAPI();

    /**
     * Read the SAML configuration field from each host and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @return Map
     */
    public Map<String, Configuration> getConfiguration()
        throws IOException, DotDataException, DotSecurityException {

        Host defaultHost    = null;
        List<Host> hosts    = hostAPI.findAllFromDB(userAPI.getSystemUser(), false);
        String fallbackSite = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_FALLBACK_SITE, null);
        Map<String, Configuration> configurationMap = new HashMap<>();

        //Verify if a fallback site is configured and get its SAML configuration
        if (fallbackSite != null && !fallbackSite.isEmpty()) {
            defaultHost = hostAPI.findByName(fallbackSite, userAPI.getSystemUser(), false);
        }

        //Save in a map the configuration for each site
        for (Host host : hosts) {
            this.populateSite(host, configurationMap, getConfigurationToUse(host, defaultHost));
        }

        return configurationMap;
    } // getConfiguration.

    /**
     * Read the SAML configuration field from the specified host and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @return Map
     */
    public Configuration getConfigurationByHost(Host host)
        throws IOException, DotDataException, DotSecurityException {

        Host defaultHost    = null;
        String fallbackSite = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_FALLBACK_SITE, null);

        //Verify if a fallback site is configured and get its SAML configuration
        if (fallbackSite != null && !fallbackSite.isEmpty()) {
            defaultHost = hostAPI.findByName(fallbackSite, userAPI.getSystemUser(), false);
        }

        //Save in a map the configuration for each site
        return this.createConfiguration(host.getHostname(), getSiteBean(getConfigurationToUse(host, defaultHost)));

    } // getConfigurationByHost.

    /**
     * Verify if a host has a configuration. Otherwise, the default one will be considered, if exists
     * (see {@link org.opensaml.saml.common.xml.SAMLConstants}.DOTCMS_SAML_FALLBACK_SITE)
     *
     * @return String with the configuration to be used (multiple lines following a key=value pattern)
     */
    private String getConfigurationToUse(Host hostToConfigure, Host fallbackHost) {
        Object hostConf = hostToConfigure.getMap().get(DotSamlConstants.DOTCMS_SAML_FIELD_NAME);

        if ((hostConf != null && !hostConf.toString().isEmpty()) || (fallbackHost != null)) {

            //if a configuration is set for the host, that one will be used
            if (hostConf != null && !hostConf.toString().isEmpty()) {
                return hostConf.toString();
            } else {
                //otherwise, a default configuration is taken from the fallback site, if exists
                hostConf = fallbackHost.getMap().get(DotSamlConstants.DOTCMS_SAML_FIELD_NAME);
                if (hostConf != null && !hostConf.toString().isEmpty()) {
                    return hostConf.toString();
                }
            }
        }
        return null;
    } // getConfigurationToUse.

    private void populateSite(Host site,
                              final Map<String, Configuration> configurationMap, String configurationToUse)
        throws DotDataException, DotSecurityException {

        SiteConfigurationBean siteBean;

        if (configurationToUse != null) {

            //save the configuration map for the host
            siteBean = getSiteBean(configurationToUse);
            final Configuration configuration = createConfiguration(site.getHostname(), siteBean);
            configurationMap.put(site.getHostname(), configuration);

            //save the same map for each host alias
            hostAPI.parseHostAliases(site).forEach(alias -> configurationMap.put(alias, configuration));
        }

    } // populateSite.

    public SiteConfigurationBean getSiteBean(String configurationToUse)
        throws DotDataException, DotSecurityException {

        SiteConfigurationBean siteBean = null;

        if (configurationToUse!=null) {

            String[] properties = configurationToUse.split(System.getProperty("line.separator"));

            final Map<String, String> siteMap = new HashMap<>();

            for (String property : properties) {
                siteMap.put(property.split("=")[0], property.split("=")[1]);
            }

            siteBean = new SiteConfigurationBean(siteMap);
        }
        return siteBean;
    } // getSiteBean.

    private Configuration createConfiguration(final String siteName,
                                              final SiteConfigurationBean siteConfigurationBean) {

        if (siteConfigurationBean != null) {

            final String configInstance = siteConfigurationBean
                .getString(DotSamlConstants.DOT_SAML_CONFIGURATION_CLASS_NAME, null);

            final Configuration configuration = InstanceUtil.newInstance
                (configInstance, DefaultDotCMSConfiguration.class, siteConfigurationBean, siteName);

            return configuration;
        }
        return null;
    }

} // E:O:F:SiteConfigurationParser.
