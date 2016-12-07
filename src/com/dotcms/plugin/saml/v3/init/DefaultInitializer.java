package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.DotSamlException;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.SiteConfigurationResolver;
import com.dotcms.plugin.saml.v3.config.*;
import com.dotcms.repackage.org.json.JSONException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default initializer
 * Responsibilities:
 *
 * - Init the Java Crypto.
 * - Init Saml Services.
 * - Init Plugin Configuration and meta data.
 *
 *
 * @author jsanca
 */
public class DefaultInitializer implements Initializer {

    private final AtomicBoolean initDone = new AtomicBoolean(false);
    private final SiteConfigurationParser siteConfigurationParser = new SiteConfigurationParser();

    @Override
    public void init(final Map<String, Object> context) {

        Logger.info(this, "Init java crypto");
        this.initJavaCrypto();

        for (Provider jceProvider : Security.getProviders()) {

            Logger.info(this, jceProvider.getInfo());
        }

        Logger.info(this, "Init Saml Services");
        this.initService();

        Logger.info(this, "Init Plugin Configuration");
        this.initConfiguration ();

        this.initDone.set(true);
    } // init.

    /**
     * Inits the app configuration.
     * The configuration By default is executed by {@link DefaultDotCMSConfiguration}
     * however you can override the implementation by your own implementation by implementing {@link Configuration}
     * and setting the classpath on the property {@link DotSamlConstants}.DOT_SAML_CONFIGURATION_CLASS_NAME
     * on the dotmarketing-config.properties
     */
    protected void initConfiguration() {

        final SiteConfigurationService siteConfigurationService;
        final Map<String, Configuration> configurationMap = new HashMap<>();
        final  Map<String, SiteConfigurationBean> configurationBeanMap;
        final String sitesConfigPath = Config.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_SITES_CONFIG_PATH,
                DotSamlConstants.DOTCMS_SAML_SITES_CONFIG_PATH_DEFAULT_VALUE
        );
        final SiteConfigurationResolver siteConfigurationResolver =
                new SiteConfigurationResolver();

        try {

            Logger.debug(this, "Parsing the json site config file: " + sitesConfigPath);

            configurationBeanMap =
                    this.siteConfigurationParser.parser(sitesConfigPath);

            Logger.debug(this, "Json Site Config parsed, result: " + configurationBeanMap);
        } catch (IOException | JSONException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        for (Map.Entry<String, SiteConfigurationBean> configEntry : configurationBeanMap.entrySet()) {

            configurationMap.put(configEntry.getKey(), this.createConfigurationBean
                    (configEntry.getKey(), configEntry.getValue()));
        }

        siteConfigurationService = new SiteConfigurationService(configurationMap);

        InstancePool.put(SiteConfigurationService.class.getName(), siteConfigurationService);
        InstancePool.put(SiteConfigurationResolver.class.getName(), siteConfigurationResolver);
    } // initConfiguration.

    public Configuration createConfigurationBean (final String siteName, final SiteConfigurationBean siteConfigurationBean) {

        final String configInstance = siteConfigurationBean
                .getString(DotSamlConstants.DOT_SAML_CONFIGURATION_CLASS_NAME, null);

        final Configuration configuration = InstanceUtil.newInstance
                (configInstance, DefaultDotCMSConfiguration.class, siteConfigurationBean, siteName);

        return configuration;
    }

    /**
     * Inits the OpenSaml service.
     */
    protected void initService() {

        try {

            Logger.info(this, "Initializing");
            InitializationService.initialize();
        } catch (InitializationException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException("Initialization failed");
        }
    } // initService.

    /**
     * Init Java Crypto stuff.
     */
    protected void initJavaCrypto() {

        final JavaCryptoValidationInitializer javaCryptoValidationInitializer
                = new JavaCryptoValidationInitializer();
        try {

            javaCryptoValidationInitializer.init();
        } catch (InitializationException e) {

            Logger.error(this, e.getMessage(), e);
        }
    } // initJavaCrypto.

    @Override
    public boolean isInitializationDone() {

        return this.initDone.get();
    } // isInitializationDone.

} // E:O:F:DefaultInitializer.
