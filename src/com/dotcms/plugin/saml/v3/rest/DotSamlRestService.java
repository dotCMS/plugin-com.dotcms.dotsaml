package com.dotcms.plugin.saml.v3.rest;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.filter.SamlFilter;
import com.dotcms.repackage.javax.ws.rs.GET;
import com.dotcms.repackage.javax.ws.rs.Path;
import com.dotcms.repackage.javax.ws.rs.PathParam;
import com.dotcms.repackage.javax.ws.rs.Produces;
import com.dotcms.repackage.javax.ws.rs.core.Context;
import com.dotcms.repackage.javax.ws.rs.core.MediaType;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.repackage.org.glassfish.jersey.server.JSONP;
import com.dotcms.rest.annotation.NoCache;
import com.dotcms.rest.exception.mapper.ExceptionMapperUtil;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Path( "/dotsaml" )
public class DotSamlRestService implements Serializable
{
	private static final long serialVersionUID = 6901877501532737335L;
	private final IdpConfigHelper idpConfigHelper;

	public DotSamlRestService()
	{
		this.idpConfigHelper = IdpConfigHelper.getInstance();
	}

	@GET
	@Path( "/login/{idpConfigId}" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Login configuration by id
	public Response login( @PathParam( "idpConfigId" ) final String idpConfigId, @Context final HttpServletRequest httpServletRequest, @Context final HttpServletResponse httpServletResponse )
	{
		Response response;

		try
		{
			final IdpConfig idpConfig = idpConfigHelper.findIdpConfig( idpConfigId );

			// If idpConfig is null, means this site does not need SAML processing
			if ( idpConfig != null && idpConfig.isEnabled() )
			{
				Logger.debug( this, "Processing saml login request for idpConfig id: " + idpConfigId );
				SamlFilter samlFilter = new SamlFilter();
				samlFilter.doRequestLoginSecurityLog( httpServletRequest, idpConfig );

				try
				{
					// This will redirect the user to the IdP Login Page.
					samlFilter.samlAuthenticationService.authentication( httpServletRequest, httpServletResponse, idpConfig );
				}
				catch ( DotSamlException | DotDataException exception )
				{
					Logger.error( this, "Error on authentication: " + exception.getMessage(), exception );
					Logger.debug( this, "Error on authentication, setting 500 response status." );
					response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
				}

				response = Response.ok().build();

			}
			else
			{
				String message = "No idpConfig for idpConfigId: " + idpConfigId + ". At " + httpServletRequest.getRequestURI();
				Logger.debug( this, message  );
				throw new DotSamlException( message  );
			}
		}
		catch ( DotSamlException dotSamlException )
		{
			Logger.error( this, dotSamlException.getMessage(), dotSamlException );
			response = ExceptionMapperUtil.createResponse( null, dotSamlException.getMessage() );
		}
		catch ( DotDataException dotDataException )
		{
			Logger.error( this, "Idp not found (" + dotDataException.getMessage() + ")", dotDataException );
			response = ExceptionMapperUtil.createResponse( null, "Idp not found (" + dotDataException.getMessage() + ")" );
		}
		catch ( IOException ioException )
		{
			Logger.error( this, "Idp is not valid (" + ioException.getMessage() + ")", ioException );
			response = ExceptionMapperUtil.createResponse( null, "Idp is not valid (" + ioException.getMessage() + ")" );
		}
		catch ( JSONException jsonException )
		{
			Logger.error( this, "Error handling json (" + jsonException.getMessage() + ")", jsonException );
			response = ExceptionMapperUtil.createResponse( null, "Error handling json (" + jsonException.getMessage() + ")" );
		}
		catch ( Exception exception )
		{
			// this is an unknown error, so we report as a 500.
			Logger.error( this, "Error getting posting idp", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
	}

	@GET
	@Path( "/metadata/{idpConfigId}" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Gets metadata configuration by id
	public void metadata( @PathParam( "idpConfigId" ) final String idpConfigId, @Context final HttpServletRequest httpServletRequest, @Context final HttpServletResponse httpServletResponse )
	{
		try
		{
			final IdpConfig idpConfig = idpConfigHelper.findIdpConfig( idpConfigId );

			// If idpConfig is null, means this site does not need SAML processing
			if ( idpConfig != null )
			{
				Logger.debug( this, "Processing saml login request for idpConfig id: " + idpConfigId );
				SamlFilter samlFilter = new SamlFilter();
				samlFilter.printMetaData( httpServletRequest, httpServletResponse, idpConfig );

			}
			else
			{
				String message = "No idpConfig for idpConfigId: " + idpConfigId + ". At " + httpServletRequest.getRequestURI();
				Logger.debug( this, message  );
				throw new DotSamlException( message  );
			}

		}
		catch ( DotSamlException dotSamlException )
		{
			Logger.error( this, dotSamlException.getMessage(), dotSamlException );
		}
		catch ( DotDataException dotDataException )
		{
			Logger.error( this, "Idp not found (" + dotDataException.getMessage() + ")", dotDataException );
		}
		catch ( IOException ioException )
		{
			Logger.error( this, "Idp is not valid (" + ioException.getMessage() + ")", ioException );
		}
		catch ( JSONException jsonException )
		{
			Logger.error( this, "Error handling json (" + jsonException.getMessage() + ")", jsonException );
		}
		catch ( Exception exception )
		{
			// this is an unknown error, so we report as a 500.
			Logger.error( this, "Error getting posting idp", exception );
		}
	}
}
