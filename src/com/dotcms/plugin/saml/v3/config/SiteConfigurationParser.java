package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.DEFAULT_SAML_CONFIG_FILE_NAME;
import static com.dotcms.plugin.saml.v3.DotSamlConstants.DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT;

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
     * Validate properties from the SAML Field.
     *
     * We need to validate these properties:
     * dotcms.saml.idp.metadata.path (File)
     * dotcms.saml.keystore.path (File)
     * dotcms.saml.keystore.password
     * dotcms.saml.keyentryid
     * dotcms.saml.keystore.entry.password
     * dotcms.saml.remove.roles.prefix
     * dotcms.saml.include.roles.pattern
     *
     * @param host
     * @throws DotDataException
     * @throws DotSecurityException
     */
    public void validateConfigurationByHost(Host host) throws DotDataException, DotSecurityException{

        Host defaultHost    = null;
        String fallbackSite = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_FALLBACK_SITE, null);

        //Verify if a fallback site is configured and get its SAML configuration
        if (fallbackSite != null && !fallbackSite.isEmpty()) {
            defaultHost = hostAPI.findByName(fallbackSite, userAPI.getSystemUser(), false);
        }

        final String configurationToUse = getConfigurationToUse(host, defaultHost);

        if ( configurationToUse != null ) {

            try {
                Properties samlProperties = new Properties();
                samlProperties.load( new StringReader(configurationToUse));

                Set<String> fieldsToValidate = new HashSet<>();
                fieldsToValidate.add(DotSamlConstants.DOTCMS_SAML_IDP_METADATA_PATH);
                fieldsToValidate.add(DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH);
                fieldsToValidate.add(DotSamlConstants.DOTCMS_SAML_KEY_STORE_PASSWORD);
                fieldsToValidate.add(DotSamlConstants.DOTCMS_SAML_KEY_ENTRY_ID);
                fieldsToValidate.add(DotSamlConstants.DOTCMS_SAML_KEY_STORE_ENTRY_PASSWORD);
                fieldsToValidate.add(DotSamlConstants.DOT_SAML_REMOVE_ROLES_PREFIX);
                fieldsToValidate.add(DotSamlConstants.DOTCMS_SAML_INCLUDE_ROLES_PATTERN);

                Set<String> missingFields = new HashSet<>();

                for (String fieldToValidate : fieldsToValidate) {
                    if ( samlProperties.getProperty(fieldToValidate) == null ) {
                        missingFields.add(fieldToValidate);
                    }
                }

                //Specific Validations for Files.
                Set<String> fileFields = new HashSet<>();
                fileFields.add(DotSamlConstants.DOTCMS_SAML_IDP_METADATA_PATH);
                fileFields.add(DotSamlConstants.DOTCMS_SAML_KEY_STORE_PATH);

                Set<String> missingFiles = new HashSet<>();

                for (String fileField : fileFields) {
                    //If field is missing we don't need to validate.
                    if ( samlProperties.getProperty(fileField) == null ) {
                        continue;
                    }

                    //Check if the file exists.
                    String filePath = samlProperties.getProperty(fileField);
                    File file = new File(filePath);
                    if ( !file.exists() && !file.canRead() ) {
                        try {
                            //Let's try with the URI.
                            URI uri = new URI(filePath);
                            file = new File(uri);
                            if ( !file.exists() && !file.canRead() ) {
                                missingFiles.add(filePath);
                            }
                        } catch (URISyntaxException e){
                            Logger.debug(this, "Problem reading file from URI: " + filePath, e);
                            missingFiles.add(filePath);
                        }
                    }
                }

                StringBuilder error = new StringBuilder();

                if ( !missingFields.isEmpty() ) {
                    error.append("Missing Fields: ");
                    error.append(org.apache.commons.lang.StringUtils.join(missingFields, ','));
                    error.append("\n");
                }
                if ( !missingFiles.isEmpty() ) {
                    error.append("Can NOT open Files: ");
                    error.append(org.apache.commons.lang.StringUtils.join(missingFiles, ','));
                }

                //If error has any message, throw the Exception with it.
                if ( UtilMethods.isSet(error.toString()) ) {
                    Logger.error(this, "Errors validating SAML Field config: " + error.toString());
                    throw new DotContentletValidationException(error.toString());
                }

            } catch (IOException e){
                throw new DotContentletValidationException("Error trying to parse SAML Field Properties", e);
            }
        }

    } // validateConfigurationByHost.

    /**
     * Verify if a host has a configuration. Otherwise, the default one will be considered, if exists
     * (see {@link org.opensaml.saml.common.xml.SAMLConstants}.DOTCMS_SAML_FALLBACK_SITE)
     *
     * @return String with the configuration to be used (multiple lines following a key=value pattern)
     */
    private String getConfigurationToUse(Host hostToConfigure, Host fallbackHost) {
        Object hostConf = hostToConfigure.getMap().get(DotSamlConstants.DOTCMS_SAML_FIELD_NAME);

        if (isValidConfiguration(hostConf) || (fallbackHost != null)) {

            //if a configuration is set for the host, that one will be used
            if (isValidConfiguration(hostConf)) {
                return hostConf.toString();
            } else {
                //otherwise, a default configuration is taken from the fallback site, if exists
                hostConf = fallbackHost.getMap().get(DotSamlConstants.DOTCMS_SAML_FIELD_NAME);
                if (isValidConfiguration(hostConf)) {
                    return hostConf.toString();
                }
            }
        }
        return null;
    } // getConfigurationToUse.

    private boolean isValidConfiguration(Object hostConf){
        return (hostConf != null && !hostConf.toString().isEmpty() && !hostConf.toString()
            .equals(DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT));
    } // isValidConfiguration

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

        if (configurationToUse!=null) {
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
