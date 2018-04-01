package com.dotcms.plugin.saml.v4.exception;

import javax.servlet.http.HttpServletResponse;

public class NotNullEmailAllowedException extends AttributesNotFoundException
{
	private static final long serialVersionUID = -3622432364873488814L;

	public NotNullEmailAllowedException()
	{
		
	}

	public NotNullEmailAllowedException( String message )
	{
		super( message );
	}

	public NotNullEmailAllowedException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public int getStatus()
	{
		return HttpServletResponse.SC_UNAUTHORIZED;
	}
}
