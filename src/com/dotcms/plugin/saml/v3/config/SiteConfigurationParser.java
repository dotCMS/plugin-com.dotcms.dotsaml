package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.content.HostService;
import com.dotcms.plugin.saml.v3.content.SamlContentTypeUtil;
import com.dotmarketing.beans.Host;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.DEFAULT_SAML_CONFIG_FILE_NAME;
import static com.dotcms.plugin.saml.v3.DotSamlConstants.DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT;

/**
 * Take a host field and creates a SiteConfigurationBean
 *
 * @author jsanca
 */
public class SiteConfigurationParser implements Serializable {

    private final SamlSiteValidator samlSiteValidator =
            new SamlSiteValidator();
    private final HostService hostService =
           (HostService) InstancePool.get(HostService.class.getName());

    /**
     * Read the SAML configuration field from each host and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @return Map
     */
    public Map<String, Configuration> getConfiguration()
            throws IOException, DotDataException, DotSecurityException {

        final List<Host> hosts = this.hostService.getAllHosts();
        return null != hosts?
                this.getConfiguration(hosts):
                Collections.emptyMap();
    } // getConfiguration.

    /**
     * Read the SAML configuration field from each host pass by parameter and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @param hosts List of Host
     * @return Map
     */
    public Map<String, Configuration> getConfiguration(final List<Host> hosts)
        throws IOException, DotDataException, DotSecurityException {

        final String fallbackSite = Config.getStringProperty
                            (DotSamlConstants.DOTCMS_SAML_FALLBACK_SITE, null);
        final Map<String, Configuration> configurationMap = new HashMap<>(); // todo: on 4.x make immutable.
        final Host defaultHost    = this.hostService.findDefaultHost(fallbackSite);

        Logger.debug(this, "The default host for saml is: " + defaultHost);

        //Save in a map the configuration for each site
        hosts.forEach(host -> this.populateSite(host, configurationMap, defaultHost));

        return configurationMap;
    } // getConfiguration.


    /**
     * Read the SAML configuration for each disable host and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @return Map
     */
    public Map<String, Configuration> getConfigurationForDisableHosts()
            throws IOException, DotDataException, DotSecurityException {

        final List<Host> hosts = this.hostService.getAllHosts();
        return null != hosts?
                this.getConfigurationForDisableHosts(hosts):
                Collections.emptyMap();
    } // getConfiguration.

    /**
     * Read the SAML configuration for each disable host and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @param hosts List of Host
     * @return Map
     */
    public Map<String, Configuration> getConfigurationForDisableHosts(final List<Host> hosts)
            throws IOException, DotDataException, DotSecurityException {

        final Map<String, Configuration> disabledConfigurationMap = new HashMap<>(); // todo: on 4.x make immutable.

        //Save in a map the configuration for each disabled site
        hosts.forEach(host -> this.populateDisabledSite(host, disabledConfigurationMap));

        return disabledConfigurationMap;
    } // getConfiguration.

    /**
     * This method will process the host configuration only if the saml config is on disabled and has the minimum configuration
     * to at least generates the meta descriptor.
     * @param site Host
     * @param disabledConfigurationMap Map
     */
    private void populateDisabledSite (final Host site,
                               final Map<String, Configuration> disabledConfigurationMap)  {


        final Map    hostProperties          = site.getMap();
        final Object hostSAMLConfiguration   = hostProperties
                .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_VELOCITY_VAR_NAME);
        final String hostSAMLAuthentication  = (String)hostProperties
                .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME);
        final boolean isDisabled             =
                SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DISABLED
                        .equalsIgnoreCase(hostSAMLAuthentication);

        try {

            //if a configuration is set for the host, that one will be used
            if  (null != hostSAMLConfiguration && isDisabled) {

                Logger.debug(this, "Doing configuration for the disabled host: " +
                                site.getHostname());
                this.samlSiteValidator.validateSiteConfiguration(site.getHostname(),
                        hostSAMLConfiguration.toString(), hostSAMLAuthentication);

                this.populateSite(site, disabledConfigurationMap, hostSAMLConfiguration.toString());
            }
        } catch (Exception e) {

            if (isDisabled) {
                Logger.error(this,
                        "Error Trying to get the disable configuration of the host: " +
                                site.getHostname() +
                                ", error message: " + e.getMessage(), e);
            }
            /*throw new DotSamlException(
                    "Error doing configuration of the host: " + hostName, e);*/
        }
    } // populateSite.


    private void populateSite (final Host host,
                               final Map<String, Configuration> configurationMap,
                               final Host defaultHost)  {

        String hostName = "Unknown";

        try {

            hostName = host.getHostname();
            Logger.debug(this, "Doing SAML Validation for the host: " + host.getHostname() );
            this.validateConfigurationByHost(host, defaultHost);
            Logger.debug(this, "Populating the host: " + host.getHostname()  + ", with the SAML Configuration");
            this.populateSite(host, configurationMap, this.getConfigurationToUse(host, defaultHost));
        } catch (Exception e) {

            Logger.error(this,
                    "Error doing configuration of the host: " +
                            hostName +
                            ", error message: " + e.getMessage(), e);
            /*throw new DotSamlException(
                    "Error doing configuration of the host: " + hostName, e);*/
        }
    } // populateSite.



    /**
     * Read the SAML configuration field from the specified host and
     * put it into a map String -> {@link SiteConfigurationBean}
     * Each configuration is read as a pattern of key=value
     *
     * @return Map
     */
    public Configuration getConfigurationByHost(final Host host)
        throws IOException, DotDataException, DotSecurityException {

        String fallbackSite       = Config.getStringProperty
                (DotSamlConstants.DOTCMS_SAML_FALLBACK_SITE, null);

        //Verify if a fallback site is configured and get its SAML configuration
        final Host defaultHost    = this.hostService.findDefaultHost(fallbackSite);

        //Save in a map the configuration for each site
        return this.createConfiguration(host.getHostname(),
                getSiteBean(getConfigurationToUse(host, defaultHost)));

    } // getConfigurationByHost.

    /**
     * Validate properties from the SAML Field.
     *
     * We need to validate these properties:
     * idp.metadata.path (File)
     * keystore.path (File)
     * keystore.password
     * keyentryid
     * keystore.entry.password
     * remove.roles.prefix
     * include.roles.pattern
     *
     * 1. Validate that the properties exist.
     * 2. Validate that the File properties exist, can access and read.
     * 3. Validate we can read the Key Store.
     *
     * @param host
     * @throws DotDataException
     * @throws DotSecurityException
     */
    public void validateConfigurationByHost(final Host host) throws DotDataException, DotSecurityException{

        final String fallbackSite = Config.getStringProperty
                (DotSamlConstants.DOTCMS_SAML_FALLBACK_SITE, null);

        //Verify if a fallback site is configured and get its SAML configuration
        final Host defaultHost    = this.hostService.findDefaultHost(fallbackSite);

        this.validateConfigurationByHost(host, defaultHost);
    }

    public void validateConfigurationByDisableHost(final Host site, final String hostSAMLAuthentication) throws DotDataException, DotSecurityException{

        final Map    hostProperties          = site.getMap();
        final Object hostSAMLConfiguration   = hostProperties
                .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_VELOCITY_VAR_NAME);

        if  (null != hostSAMLConfiguration) {

            Logger.debug(this, "Doing validation for the disabled host: " +
                    site.getHostname());
            this.samlSiteValidator.validateSiteConfiguration(site.getHostname(),
                    hostSAMLConfiguration.toString(), hostSAMLAuthentication);
        }
    }

    /**
     * Validate properties from the SAML Field.
     *
     * We need to validate these properties:
     * idp.metadata.path (File)
     * keystore.path (File)
     * keystore.password
     * keyentryid
     * keystore.entry.password
     * remove.roles.prefix
     * include.roles.pattern
     *
     * 1. Validate that the properties exist.
     * 2. Validate that the File properties exist, can access and read.
     * 3. Validate we can read the Key Store.
     *
     * Assumes you send the defaultHost.
     *
     * @param host Host
     * @param defaultHost Host
     * @throws DotDataException
     * @throws DotSecurityException
     */
    public void validateConfigurationByHost(final Host host,
                                            final Host defaultHost) throws DotDataException, DotSecurityException{

        final String hostSAMLConfiguration =
                getConfigurationToUse(host, defaultHost);
        final String hostSAMLAuthentication  = (String)host.getMap()
                .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME);

        Logger.debug(this, "Doing validateConfigurationByHost, hostSAMLConfiguration: " +
                                hostSAMLConfiguration);
        this.samlSiteValidator.validateSiteConfiguration(host.getHostname(),
                hostSAMLConfiguration, hostSAMLAuthentication);
    } // validateConfigurationByHost.

    /**
     * Verify if a host has a configuration. Otherwise, the default one will be considered, if exists
     * (see {@link org.opensaml.saml.common.xml.SAMLConstants}.DOTCMS_SAML_FALLBACK_SITE)
     *
     * @return String with the configuration to be used (multiple lines following a key=value pattern)
     */
    private String getConfigurationToUse(final Host hostToConfigure, final Host fallbackHost) {

        final Map    hostProperties          = hostToConfigure.getMap();
        final Object hostSAMLConfiguration   = hostProperties
                .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_VELOCITY_VAR_NAME);
        final String hostSAMLAuthentication  = (String)hostProperties
                .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME);
        final boolean isEnabled              =
                SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_ENABLED
                        .equalsIgnoreCase(hostSAMLAuthentication);
        final boolean isDefault              =
                SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DEFAULT
                        .equalsIgnoreCase(hostSAMLAuthentication);

        Logger.debug(this, "For the Host: " + hostToConfigure.getHostname()  +
             " the hostProperties are " + hostProperties);

        Logger.debug(this, "For the Host: " + hostToConfigure.getHostname()  +
                    " the SAML Authentication is: " + hostSAMLAuthentication +
                    ", going to validate their configuration...");

                //if a configuration is set for the host, that one will be used
        return isEnabled && isValidConfiguration(hostSAMLConfiguration)?
                this.getHostConfiguration(hostSAMLConfiguration, hostToConfigure.getHostname()):
                //otherwise, a default configuration is taken from the fallback site, if exists
                this.getFallbackHostConfiguration (fallbackHost, isDefault, hostToConfigure.getHostname());
    } // getConfigurationToUse.

    private String getHostConfiguration (final Object hostSAMLConfiguration,
                                         final String hostName) {

        Logger.debug(this, "For the Host: " + hostName  +
                " the SAML Configuration is valid going to use this configuration: " + hostSAMLConfiguration);

        return hostSAMLConfiguration.toString();
    }

    private String getFallbackHostConfiguration(final Host fallbackHost,
                                                final boolean useDefaultConfiguration,
                                                final String hostName) {

        String fallbackHostConfiguration     = null;
        final boolean isNotNullFallbackHost  = (null != fallbackHost);

        Logger.debug(this, "For the Host: " + hostName  +
                " Trying the fallback host configuration, useDefaultConfiguration = " + useDefaultConfiguration +
                ", and the fallbackHost is " + (isNotNullFallbackHost? "not null": "null") );

        if (useDefaultConfiguration && isNotNullFallbackHost) {

            final Object hostSAMLConfiguration = fallbackHost.getMap()
                    .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_VELOCITY_VAR_NAME);
            final String hostSAMLAuthentication  = (String)fallbackHost.getMap()
                    .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME);
            final boolean isEnabled              =
                    SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_ENABLED
                            .equalsIgnoreCase(hostSAMLAuthentication);

            Logger.debug(this, "For the Host: " + hostName  +
                    "The fallback host is " + fallbackHost.getHostname() +
                    "The fallback configuration is " + (isEnabled? "Enabled":"Disable") +
                    ", and the fallbackHost configuration is " + hostSAMLConfiguration );

            fallbackHostConfiguration =
                    isEnabled && isValidConfiguration(hostSAMLConfiguration) ?
                        hostSAMLConfiguration.toString() : null;

            Logger.debug(this, "For the Host: " + hostName  +
                    ", configuration used will be: " + fallbackHostConfiguration );
        }

        return fallbackHostConfiguration;
    } // getFallbackHostConfiguration.

    private boolean isValidConfiguration(Object hostConf) {
        return (hostConf != null && !hostConf.toString().isEmpty() && !hostConf.toString()
            .equals(DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT));
    } // isValidConfiguration

    private void populateSite(final Host site,
                              final Map<String, Configuration> configurationMap,
                              final String configurationToUse)
        throws DotDataException, DotSecurityException {

        SiteConfigurationBean siteBean;

        if (configurationToUse != null && !configurationToUse.isEmpty()) {

            Logger.info(this, "Populating the configuration for the host: " + site);
            // save the configuration map for the host
            siteBean = this.getSiteBean(configurationToUse);
            final Configuration configuration =
                    this.createConfiguration(site.getHostname(), siteBean);
            configurationMap.put(site.getHostname(), configuration);

            Logger.info(this, "Populated the site with the configuration: " + configuration +
                    ", for the host: " + site);
            //save the same map for each host alias
            this.hostService.getHostAlias(site).forEach(alias -> configurationMap.put(alias, configuration));
        }
    } // populateSite.

    /**
     * Read the default configuration file, override properties with values from site/host SAML field and
     * create {@link SiteConfigurationBean} from there.
     *
     * @param configurationToUse
     * @return
     * @throws DotDataException
     * @throws DotSecurityException
     */
    public SiteConfigurationBean getSiteBean(String configurationToUse)
        throws DotDataException, DotSecurityException {

        SiteConfigurationBean siteBean = null;

        if (configurationToUse != null) {
            Properties samlProperties = new Properties();

            //Reading default SAML properties.
            try {
                samlProperties.load( this.getClass().getClassLoader().getResourceAsStream(DEFAULT_SAML_CONFIG_FILE_NAME) );
            } catch (IOException e) {
                Logger.warn(this, "Error reading dotCMS default properties file.");
            }

            //Reading SAML properties from the SAML filed on the host,
            //these properties will override the default ones.
            try {
                samlProperties.load( new StringReader(configurationToUse));
            } catch (IOException e) {
                Logger.error(this, "Error reading SAML properties from Site Field.", e);
            }

            final Map<String, String> siteMap = new HashMap<>();

            for (final String key : samlProperties.stringPropertyNames()) {
                siteMap.put(key, samlProperties.getProperty(key));
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
