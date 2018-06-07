package com.dotcms.plugin.saml.v3.init;

import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.rest.DotSamlRestService;
import com.dotcms.repackage.org.tuckey.web.filters.urlrewrite.NormalRule;
import com.dotmarketing.filters.DotUrlRewriteFilter;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default initializer Responsibilities: - Init the Java Crypto. - Init Saml
 * Services. - Init Plugin Configuration and meta data.
 *
 * @author jsanca
 */
public class SamlInitializer implements Initializer
{

	private static final long serialVersionUID = -5869927082029401479L;

	private final AtomicBoolean initDone = new AtomicBoolean( false );

	public SamlInitializer()
	{

	}

	@Override
	public void init( final Map<String, Object> context )
	{
		JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer();

		try
		{
			javaCryptoValidationInitializer.init();
		}
		catch ( InitializationException initializationException )
		{
			initializationException.printStackTrace();
		}

		for ( Provider jceProvider : Security.getProviders() )
		{
			Logger.info( this, jceProvider.getInfo() );
		}

		try
		{
			// Force a cache/file system read to initialize cache.
			IdpConfigHelper.getInstance().getIdpConfigs();
		}
		catch ( JSONException | IOException exception )
		{
			Logger.error( this, "Could not initialize IdpConfigs.", exception );
		}

		try
		{
			Logger.info( this, "Initializing" );
			InitializationService.initialize();
			
			if (XMLObjectProviderRegistrySupport.getParserPool() == null ) {
				XMLObjectProviderRegistrySupport.setParserPool(new BasicParserPool());
			}
		}
		catch ( InitializationException e )
		{
			throw new RuntimeException( "Initialization failed", e );
		}

		// Tuckey rewrite to route /dotsaml/login/* to /api/dotsaml/login/*
		// and /dotsaml/metadata/* to /api/dotsaml/metadata/*
		addDotsamlRestServiceRedirect();

		this.initDone.set( true );
	}

	private void addDotsamlRestServiceRedirect() {
		NormalRule rule = new NormalRule();
		rule.setFrom("^\\/dotsaml\\/("+String.join("|", DotSamlRestService.dotsamlPathSegments)+")\\/(.+)$");
		rule.setToType("forward");
		rule.setTo("/api/dotsaml/$1/$2");
		rule.setName("Dotsaml REST Service Redirect");
		DotUrlRewriteFilter urlRewriteFilter = DotUrlRewriteFilter.getUrlRewriteFilter();
		try {
			if(urlRewriteFilter != null) {
                urlRewriteFilter.addRule(rule);
            }else {
				throw new Exception();
            }
		} catch (Exception e) {
			Logger.error(this, "Could not add Dotsaml REST Service Redirect.  Reqeusts to /dotsaml/login/{UUID} will fail!");
		}
	}

	@Override
	public boolean isInitializationDone()
	{

		return this.initDone.get();
	}

}
