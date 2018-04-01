package com.dotcms.plugin.saml.v4;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.util.Logger;

/**
 * Provides the default host to be used as a service provider Created by
 * nollymar on 3/21/17.
 */
public class SPIIssuerResolver
{
	public static Host getDefaultServiceProviderIssuer()
	{
		HostAPI hostAPI = APILocator.getHostAPI();

		try
		{
			return hostAPI.findDefaultHost( APILocator.getUserAPI().getSystemUser(), false );
		}
		catch ( DotDataException | DotSecurityException e )
		{
			Logger.error( SPIIssuerResolver.class, e.getMessage(), e );
		}

		return null;
	}
}
