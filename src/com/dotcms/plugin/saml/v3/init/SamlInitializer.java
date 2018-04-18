package com.dotcms.plugin.saml.v3.init;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import com.dotmarketing.util.Logger;

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
public class SamlInitializer implements Initializer {

	private static final long serialVersionUID = -5869927082029401479L;
	
	private final AtomicBoolean initDone = new AtomicBoolean(false);

    public SamlInitializer(){

    }

    @Override
    public void init(final Map<String, Object> context) {
    	
    	JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer();
        try {
            javaCryptoValidationInitializer.init();
        } catch (InitializationException e) {
            e.printStackTrace();
        }

        for (Provider jceProvider : Security.getProviders()) {
            Logger.info(this, jceProvider.getInfo());
        }

        try {
            Logger.info(this, "Initializing");
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException("Initialization failed", e);
        }

        this.initDone.set(true);
    } 


    @Override
    public boolean isInitializationDone() {

        return this.initDone.get();
    } 

}
