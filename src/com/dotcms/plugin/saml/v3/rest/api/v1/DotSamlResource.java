package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.config.IdpConfigWriterReader;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.filter.SamlFilter;
import com.dotcms.plugin.saml.v3.util.pagination.IdpConfigPaginator;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;

import com.dotcms.rest.InitDataObject;
import com.dotcms.rest.ResponseEntityView;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.annotation.NoCache;
import com.dotcms.rest.exception.mapper.ExceptionMapperUtil;
import com.dotcms.util.CollectionsUtils;
import com.dotcms.util.PaginationUtil;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.portal.model.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

@Path( "/v1/dotsaml" )
public class DotSamlResource implements Serializable
{
	private static final long serialVersionUID = 8015545653539491684L;
	private final IdpConfigHelper idpConfigHelper;
	private final WebResource webResource;
	private final PaginationUtil paginationUtil;

	public DotSamlResource()
	{
		this.idpConfigHelper = IdpConfigHelper.getInstance();
		this.webResource = new WebResource();
		this.paginationUtil = new PaginationUtil( new IdpConfigPaginator() );
	}

	@SuppressWarnings( "unchecked" )
	@POST
	@Path( "/idp" )
	@JSONP
	@NoCache
	@Consumes( MediaType.MULTIPART_FORM_DATA )
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Saves an idp config.
	public final Response createIdpConfig( @Context final HttpServletRequest request, @FormDataParam( "id" ) String id, @FormDataParam( "idpName" ) String idpName, @FormDataParam( "enabled" ) boolean enabled, @FormDataParam( "sPIssuerURL" ) String sPIssuerURL, @FormDataParam( "sPEndpointHostname" ) String sPEndpointHostname, @FormDataParam( "privateKey" ) InputStream privateKeyStream, @FormDataParam( "privateKey" ) FormDataContentDisposition privateKeyFileDetail, @FormDataParam( "publicCert" ) InputStream publicCertStream, @FormDataParam( "publicCert" ) FormDataContentDisposition publicCertFileDetail, @FormDataParam( "idPMetadataFile" ) InputStream idPMetadataFileStream, @FormDataParam( "idPMetadataFile" ) FormDataContentDisposition idPMetadataFileDetail, @FormDataParam( "signatureValidationType" ) String signatureValidationType, @FormDataParam( "optionalProperties" ) String optionalProperties, @FormDataParam( "sites" ) String sites )
	{
		this.webResource.init( null, true, request, true, null );

		Response response;

		try
		{
			IdpConfig idpConfig;

			if ( UtilMethods.isSet( id ) )
			{
				idpConfig = idpConfigHelper.findIdpConfig( id );
			}
			else
			{
				idpConfig = new IdpConfig();
			}

			idpConfig.setIdpName( idpName );
			idpConfig.setEnabled( enabled );
			idpConfig.setSpIssuerURL( sPIssuerURL );
			idpConfig.setSpEndpointHostname( sPEndpointHostname );

			if ( UtilMethods.isSet( privateKeyFileDetail ) && UtilMethods.isSet( privateKeyFileDetail.getFileName() ) )
			{
				File privateKey = File.createTempFile( "privateKey", "key" );
				FileUtils.copyInputStreamToFile( privateKeyStream, privateKey );
				idpConfig.setPrivateKey( privateKey );
			}

			if ( UtilMethods.isSet( publicCertFileDetail ) && UtilMethods.isSet( publicCertFileDetail.getFileName() ) )
			{
				File publicCert = File.createTempFile( "publicCert", "crt" );
				FileUtils.copyInputStreamToFile( publicCertStream, publicCert );
				idpConfig.setPublicCert( publicCert );
			}

			if ( UtilMethods.isSet( idPMetadataFileDetail ) && UtilMethods.isSet( idPMetadataFileDetail.getFileName() ) )
			{
				File idPMetadataFile = File.createTempFile( "idPMetadataFile", "xml" );
				FileUtils.copyInputStreamToFile( idPMetadataFileStream, idPMetadataFile );
				idpConfig.setIdPMetadataFile( idPMetadataFile );
			}

			idpConfig.setSignatureValidationType( signatureValidationType );

			if ( UtilMethods.isSet( optionalProperties ) )
			{
				final Properties parsedProperties = new Properties();
				parsedProperties.load( new StringReader( optionalProperties ) );
				idpConfig.setOptionalProperties( parsedProperties );
			}

			HashMap<String, String> sitesMap = new ObjectMapper().readValue( sites, HashMap.class );
			idpConfig.setSites( sitesMap );

			idpConfig = idpConfigHelper.saveIdpConfig( idpConfig );

			response = Response.ok( new ResponseEntityView( idpConfig ) ).build();
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

	@DELETE
	@Path( "/idp/{id}" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Deletes an idp config.
	public Response deleteIdpConfig( @PathParam( "id" ) final String id, @Context final HttpServletRequest request )
	{
		this.webResource.init( null, true, request, true, null );

		Response response;

		try
		{
			IdpConfig idpConfig = new IdpConfig();
			idpConfig.setId( id );

			idpConfigHelper.deleteIdpConfig( idpConfig );

			response = Response.ok( new ResponseEntityView( CollectionsUtils.map( "deleted", id ) ) ).build();
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
			Logger.error( this, "Error deleting idps", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
	}

	@GET
	@Path( "/default" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Gets default idp config id
	public Response getDefault( @Context final HttpServletRequest request )
	{
		this.webResource.init( null, true, request, true, null );

		Response response;

		try
		{
			final String defaultIdpConfigId = idpConfigHelper.getDefaultIdpConfigId();

			response = Response.ok( new ResponseEntityView( CollectionsUtils.map( IdpConfigWriterReader.DEFAULT_SAML_CONFIG, defaultIdpConfigId ) ) ).build();
		}
		catch ( IOException ioException )
		{
			Logger.error( this, "Error reading file with Idps (" + ioException.getMessage() + ")", ioException );
			response = ExceptionMapperUtil.createResponse( null, "Idp is not valid (" + ioException.getMessage() + ")" );
		}
		catch ( JSONException jsonException )
		{
			Logger.error( this, "Error handling json with Idps (" + jsonException.getMessage() + ")", jsonException );
			response = ExceptionMapperUtil.createResponse( null, "Error handling json (" + jsonException.getMessage() + ")" );
		}
		catch ( Exception exception )
		{
			// this is an unknown error, so we report as a 500.
			Logger.error( this, "Error getting default idp", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
	}

	@GET
	@Path( "/disabledsites" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Gets disabled sites map
	public Response getDisabledSites( @Context final HttpServletRequest request )
	{
		this.webResource.init( null, true, request, true, null );

		Response response;

		try
		{
			final Map<String, String> disabledSiteIds = idpConfigHelper.getDisabledSiteIds();

			response = Response.ok( new ResponseEntityView( CollectionsUtils.map( IdpConfigWriterReader.DISABLE_SAML_SITES, disabledSiteIds ) ) ).build();
		}
		catch ( IOException ioException )
		{
			Logger.error( this, "Error reading file with disabled sites (" + ioException.getMessage() + ")", ioException );
			response = ExceptionMapperUtil.createResponse( null, "disable site is not valid (" + ioException.getMessage() + ")" );
		}
		catch ( JSONException jsonException )
		{
			Logger.error( this, "Error handling json with Idps (" + jsonException.getMessage() + ")", jsonException );
			response = ExceptionMapperUtil.createResponse( null, "Error handling disabled site json (" + jsonException.getMessage() + ")" );
		}
		catch ( Exception exception )
		{
			// this is an unknown error, so we report as a 500.
			Logger.error( this, "Error getting default idp", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
	}

	@GET
	@Path( "/idp/{id}" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Gets an idp configuration by id
	public Response getIdp( @PathParam( "id" ) final String id, @Context final HttpServletRequest request )
	{
		Response response;

		try
		{
			final IdpConfig idpConfig = idpConfigHelper.findIdpConfig( id );
			response = Response.ok( new ResponseEntityView( idpConfig ) ).build();
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
	@Path( "/idps" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Gets paginated list of all idp configurations
	public final Response getIdps( @Context final HttpServletRequest request, @QueryParam( PaginationUtil.FILTER ) final String filter, @QueryParam( PaginationUtil.PAGE ) final int page, @QueryParam( PaginationUtil.PER_PAGE ) final int perPage, @DefaultValue( "upper(name)" ) @QueryParam( PaginationUtil.ORDER_BY ) String orderbyParam, @DefaultValue( "ASC" ) @QueryParam( PaginationUtil.DIRECTION ) String direction )
	{
		final InitDataObject initData = this.webResource.init( null, true, request, true, null );
		final User user = initData.getUser();

		Response response;

		try
		{
			response = this.paginationUtil.getPage( request, user, filter, page, perPage, orderbyParam, direction );
		}
		catch ( Exception exception )
		{
			// this is an unknown error, so we report as a 500.
			Logger.error( this, "Error getting idps", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
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

	@SuppressWarnings( "unchecked" )
	@POST
	@Path( "/disabledsites" )
	@JSONP
	@NoCache
	@Consumes( MediaType.MULTIPART_FORM_DATA )
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Save disabled sites map
	public final Response saveDisabledSited( @Context final HttpServletRequest request, @FormDataParam( "disabledsites" ) String disabledSites )
	{
		this.webResource.init( null, true, request, true, null );

		Response response;

		try
		{
			HashMap<String, String> disabledSitesMap = new ObjectMapper().readValue( disabledSites, HashMap.class );
			idpConfigHelper.saveDisabledSiteIds( disabledSitesMap );

			response = Response.ok().build();
		}
		catch ( IOException ioException )
		{
			Logger.error( this, "Error reading file with disabled sites (" + ioException.getMessage() + ")", ioException );
			response = ExceptionMapperUtil.createResponse( null, "disable site is not valid (" + ioException.getMessage() + ")" );
		}
		catch ( JSONException jsonException )
		{
			Logger.error( this, "Error handling json with Idps (" + jsonException.getMessage() + ")", jsonException );
			response = ExceptionMapperUtil.createResponse( null, "Error handling disabled site json (" + jsonException.getMessage() + ")" );
		}
		catch ( Exception exception )
		{
			// this is an unknown error, so we report as a 500.
			Logger.error( this, "Error getting default idp", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
	}

	@POST
	@Path( "/default/{id}" )
	@JSONP
	@NoCache
	@Produces( { MediaType.APPLICATION_JSON, "application/javascript" } )
	// Sets default idp configuration id
	public Response setDefault( @PathParam( "id" ) final String id, @Context final HttpServletRequest request )
	{
		this.webResource.init( null, true, request, true, null );

		Response response;

		try
		{
			idpConfigHelper.setDefaultIdpConfig( id );

			response = Response.ok( new ResponseEntityView( CollectionsUtils.map( "default", id ) ) ).build();
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
			Logger.error( this, "Error getting setting idp", exception );
			response = ExceptionMapperUtil.createResponse( exception, Response.Status.INTERNAL_SERVER_ERROR );
		}

		return response;
	}
}
