package com.dotcms.plugin.saml.v4.exception;

/**
 * Exception to report things related to the dot saml exception
 * 
 * @author jsanca
 */
public class DotSamlException extends RuntimeException
{
	private static final long serialVersionUID = -3569526825729783600L;

	public DotSamlException()
	{
		
	}

	public DotSamlException( String message )
	{
		super( message );
	}

	public DotSamlException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
