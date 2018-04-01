package com.dotcms.plugin.saml.v4.exception;

import com.dotcms.repackage.javax.ws.rs.core.Response.Status;

/**
 * To report unauthorized issues.
 * 
 * @author jsanca
 */
public class SamlUnauthorizedException extends DotSamlException
{
	private static final long serialVersionUID = 2827175662161844965L;
	private final int status;
	private final String unauthorizedPage;

	public SamlUnauthorizedException( final String message )
	{
		this( message, Status.UNAUTHORIZED.getStatusCode(), "/html/error/custom-error-page.jsp" );
	}

	public SamlUnauthorizedException( final String message, final Throwable cause )
	{
		this( message, cause, Status.UNAUTHORIZED.getStatusCode(), "/html/error/custom-error-page.jsp" );
	}

	public SamlUnauthorizedException( String message, int status, String unauthorizedPage )
	{
		super( message );
		this.status = status;
		this.unauthorizedPage = unauthorizedPage;
	}

	public SamlUnauthorizedException( String message, Throwable cause, int status, String unauthorizedPage )
	{
		super( message, cause );
		this.status = status;
		this.unauthorizedPage = unauthorizedPage;
	}

	public int getStatus()
	{
		return status;
	}

	public String getUnauthorizedPage()
	{
		return unauthorizedPage;
	}
}
