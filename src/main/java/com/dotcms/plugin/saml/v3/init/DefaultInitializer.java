package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.DotSamlException;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default initializer
 * @author jsanca
 */
public class DefaultInitializer implements Initializer {

    private AtomicBoolean initDone = new AtomicBoolean(false);

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

    protected void initConfiguration() {

        final String configInstance = Config.getStringProperty(
                DotSamlConstants.DOT_SAML_CONFIGURATION_CLASS_NAME, null
        );

        final Configuration configuration = InstanceUtil.newInstance
                (configInstance, DefaultDotCMSConfiguration.class);

        InstancePool.put(Configuration.class.getName(), configuration);
    } // initConfiguration.

    protected void initService() {

        try {

            Logger.info(this, "Initializing");
            InitializationService.initialize();
        } catch (InitializationException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException("Initialization failed");
        }
    }

    protected void initJavaCrypto() {

        final JavaCryptoValidationInitializer javaCryptoValidationInitializer
                = new JavaCryptoValidationInitializer();
        try {

            javaCryptoValidationInitializer.init();
        } catch (InitializationException e) {

            Logger.error(this, e.getMessage(), e);
        }
    }

    @Override
    public boolean isInitializationDone() {

        return this.initDone.get();
    } // isInitializationDone.

} // E:O:F:DefaultInitializer.
