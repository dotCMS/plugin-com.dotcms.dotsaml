package com.dotcms.plugin.saml.v3.config;

/**
 * Exception to report an issue with the issuer value.
 * 
 * @author jsanca
 */

/**
 * Identified as part of Version 3 SAML configuration.  Will most likely be removed or replaced.
 *
 * @deprecated 
 */
@Deprecated
public class InvalidIssuerValueException extends RuntimeException
{
	private static final long serialVersionUID = 2963820217308468676L;

	public InvalidIssuerValueException( String message )
	{
		super( message );
	}

}
