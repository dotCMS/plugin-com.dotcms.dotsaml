package com.dotcms.plugin.saml.v3.handler;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import com.liferay.util.InstancePool;

import java.io.Serializable;

/**
 * A factory for the {@link AssertionResolverHandler}
 * 
 * @author jsanca
 */
public class AssertionResolverHandlerFactory implements Serializable
{
	private static final long serialVersionUID = 2434118681822205248L;

	/**
	 * Get the resolver assertion depending on the site.
	 * 
	 * @param idpConfig {@link IdpConfig}
	 * @return
	 */
	public AssertionResolverHandler getAssertionResolverForSite( final IdpConfig idpConfig )
	{
		String className = null;

		try
		{
			className = (String) idpConfig.getOptionString( DotSamlConstants.DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME );
		}
		catch ( Exception exception )
		{
			Logger.info( this, "Optional property not set: " + DotSamlConstants.DOTCMS_SAML_ASSERTION_RESOLVER_HANDLER_CLASS_NAME + " for idpConfig: " + idpConfig.getId() + " Using default." );
		}

		final AssertionResolverHandler assertionResolverHandler = ( !UtilMethods.isSet( className ) ) ? this.getDefaultAssertionResolverHandler() : this.getAssertionResolverHandler( className );

		Logger.debug( this, "Getting the assertion resolver for the idpConfig: " + idpConfig.getId() + ", with the class: " + assertionResolverHandler );

		return assertionResolverHandler;
	}

	private AssertionResolverHandler getDefaultAssertionResolverHandler()
	{
		return this.getAssertionResolverHandler( HttpPostAssertionResolverHandlerImpl.class.getName() );
	}

	private AssertionResolverHandler getAssertionResolverHandler( final String className )
	{
		return (AssertionResolverHandler) InstancePool.get( className );
	}
}
