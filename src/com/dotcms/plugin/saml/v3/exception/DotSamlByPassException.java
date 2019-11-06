package com.dotcms.plugin.saml.v3.exception;

/**
 * Exception to report no SAML configuration
 * 
 * @author tstave
 */
public class DotSamlByPassException extends RuntimeException
{
	private static final long serialVersionUID = -3569526825729783600L;

	public DotSamlByPassException()
	{
		
	}

	public DotSamlByPassException( String message )
	{
		super( message );
	}

	public DotSamlByPassException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
