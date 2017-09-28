package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.SiteConfigurationResolver;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationService;
import com.dotcms.plugin.saml.v3.content.SamlContentTypeUtil;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.hooks.SamlHostPostHook;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.Interceptor;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.structure.business.StructureAPI;
import com.dotmarketing.portlets.structure.factories.FieldFactory;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.services.StructureServices;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT;

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
    private final SamlContentTypeUtil samlContentTypeUtil;

    public DefaultInitializer(){

        this(new SamlContentTypeUtil());
    }

    @VisibleForTesting
    public DefaultInitializer(final SamlContentTypeUtil samlContentTypeUtil) {

        this.samlContentTypeUtil = samlContentTypeUtil;
    }

    @Override
    public void init(final Map<String, Object> context) {

        Logger.info(this, "About to create SAML field under Host Content Type");
        this.createSAMLFields();
        SamlHostPostHook postHook = new SamlHostPostHook();
        Interceptor interceptor = (Interceptor)APILocator.getContentletAPIntercepter();
        interceptor.delPostHookByClassName(postHook.getClass().getName());
        try {
            interceptor.addPostHook(postHook);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            Logger.error(this, "Error adding SamlHostPostHook", e);
        }

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
     * 1. Get the Host Structure.
     * 2. Create a SAML field if the Structure doesn't have one.
     *
     * We need a SAML field(textare) under the Host structure in order
     * to have a place to configure SAML for each Site.
     */
    private void createSAMLFields() {

        this.samlContentTypeUtil.checkORCreateSAMLField();
    }// createSAMLFields.


    /**
     * Inits the app configuration.
     * The configuration By default is executed by {@link DefaultDotCMSConfiguration}
     * however you can override the implementation by your own implementation by implementing {@link Configuration}
     * and setting the classpath on the property {@link DotSamlConstants}.DOT_SAML_CONFIGURATION_CLASS_NAME
     * on the dotmarketing-config.properties
     */
    protected void initConfiguration() {

        final SiteConfigurationService siteConfigurationService;
        final Map<String, Configuration> configurationMap;

        final SiteConfigurationResolver siteConfigurationResolver =
                new SiteConfigurationResolver();

        try {

            configurationMap =
                    this.siteConfigurationParser.getConfiguration();

        } catch (IOException | DotDataException | DotSecurityException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        siteConfigurationService = new SiteConfigurationService(configurationMap);

        InstancePool.put(SiteConfigurationService.class.getName(), siteConfigurationService);
        InstancePool.put(SiteConfigurationResolver.class.getName(), siteConfigurationResolver);
    } // initConfiguration.



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
