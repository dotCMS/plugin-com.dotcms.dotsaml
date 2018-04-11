package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotcms.plugin.saml.v3.util.pagination.IdpConfigPaginator;
import com.dotcms.repackage.javax.ws.rs.*;
import com.dotcms.repackage.javax.ws.rs.core.Context;
import com.dotcms.repackage.javax.ws.rs.core.MediaType;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.repackage.org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import com.dotcms.repackage.org.glassfish.jersey.media.multipart.FormDataParam;
import com.dotcms.repackage.org.glassfish.jersey.server.JSONP;
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
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Path("/v1/dotsaml")
public class DotSamlResource implements Serializable {

    private final IdpConfigHelper idpConfigHelper;
    private final WebResource webResource;
    private final PaginationUtil paginationUtil;

    public DotSamlResource() {
        this.idpConfigHelper = IdpConfigHelper.getInstance();
        this.webResource = new WebResource();
        this.paginationUtil = new PaginationUtil(new IdpConfigPaginator());
    }

    @GET
    @Path("/idps")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response getIdps(@Context final HttpServletRequest request,
                                  @QueryParam(PaginationUtil.FILTER)   final String filter,
                                  @QueryParam(PaginationUtil.PAGE) final int page,
                                  @QueryParam(PaginationUtil.PER_PAGE) final int perPage,
                                  @DefaultValue("upper(name)") @QueryParam(PaginationUtil.ORDER_BY) String orderbyParam,
                                  @DefaultValue("ASC") @QueryParam(PaginationUtil.DIRECTION) String direction) {
        final InitDataObject initData = this.webResource.init(null, true, request, true, null);
        final User user = initData.getUser();

        Response response;

        try {
            response = this.paginationUtil.getPage(request, user, filter, page, perPage, orderbyParam, direction);

        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting idps", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // getIdps.

    @GET
    @Path("/idp/{id}")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public Response getIdp(@PathParam("id") final String id, @Context final HttpServletRequest req){
        final InitDataObject initData = this.webResource.init(null, false, req, false, null);
        Response response;

        try{
            final IdpConfig idpConfig = idpConfigHelper.findIdpConfig(id);
            response = Response.ok(new ResponseEntityView(idpConfig)).build();
        } catch (DotDataException e) {
            Logger.error(this,"Idp not found (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp not found (" + e.getMessage() + ")");
        } catch (IOException e) {
            Logger.error(this,"Idp is not valid (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting posting idp", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } //getIdp.

    @POST
    @Path("/idp")
    @JSONP
    @NoCache
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response createIdpConfig(@Context final HttpServletRequest req,
                                          @FormDataParam("id") String id,
                                          @FormDataParam("idpName") String idpName,
                                          @FormDataParam("enabled") boolean enabled,
                                          @FormDataParam("sPIssuerURL") String sPIssuerURL,
                                          @FormDataParam("sPEndponintHostname") String sPEndponintHostname,
                                          @FormDataParam("privateKey") InputStream privateKeyStream,
                                          @FormDataParam("privateKey") FormDataContentDisposition privateKeyFileDetail,
                                          @FormDataParam("publicCert") InputStream publicCertStream,
                                          @FormDataParam("publicCert") FormDataContentDisposition publicCertFileDetail,
                                          @FormDataParam("idPMetadataFile") InputStream idPMetadataFileStream,
                                          @FormDataParam("idPMetadataFile") FormDataContentDisposition idPMetadataFileDetail,
                                          @FormDataParam("signatureValidationType") String signatureValidationType,
                                          @FormDataParam("optionalProperties") String optionalProperties,
                                          @FormDataParam("sites") String sites) {
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            IdpConfig.Builder idpBuilder;

            if (UtilMethods.isSet(id)){
                idpBuilder = IdpConfig.convertIdpConfigToBuilder(idpConfigHelper.findIdpConfig(id));
            } else {
                idpBuilder = new IdpConfig.Builder();
            }

            idpBuilder.idpName(idpName);
            idpBuilder.enabled(enabled);
            idpBuilder.sPIssuerURL(sPIssuerURL);
            idpBuilder.sPEndponintHostname(sPEndponintHostname);

            if (UtilMethods.isSet(privateKeyFileDetail) && UtilMethods.isSet(privateKeyFileDetail.getFileName())){
                File privateKey = File.createTempFile("privateKey", "key");
                Files.copy(privateKeyStream, privateKey.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(privateKeyStream);
                idpBuilder.privateKey(privateKey);
            }
            if (UtilMethods.isSet(publicCertFileDetail) && UtilMethods.isSet(publicCertFileDetail.getFileName())){
                File publicCert = File.createTempFile("publicCert", "crt");
                Files.copy(publicCertStream, publicCert.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(publicCertStream);
                idpBuilder.publicCert(publicCert);
            }
            if (UtilMethods.isSet(idPMetadataFileDetail) && UtilMethods.isSet(idPMetadataFileDetail.getFileName())){
                File idPMetadataFile = File.createTempFile("idPMetadataFile", "xml");
                Files.copy(idPMetadataFileStream, idPMetadataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                IOUtils.closeQuietly(idPMetadataFileStream);
                idpBuilder.idPMetadataFile(idPMetadataFile);
            }

            idpBuilder.signatureValidationType(signatureValidationType);

            if (UtilMethods.isSet(optionalProperties)){
                final Properties parsedProperties = new Properties();
                parsedProperties.load(new StringReader(optionalProperties));
                idpBuilder.optionalProperties(parsedProperties);
            }

            HashMap<String, String> sitesMap = new ObjectMapper().readValue(sites, HashMap.class);
            idpBuilder.sites(sitesMap);

            IdpConfig idpConfig = idpBuilder.build();
            idpConfig = idpConfigHelper.saveIdpConfig(idpConfig);

            response = Response.ok(new ResponseEntityView(idpConfig)).build();
        } catch (IOException e) {
            Logger.error(this,"Idp is not valid (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting posting idp", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // createIdpConfig.

    @DELETE
    @Path("/idp/{id}")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public Response deleteIdpConfig(@PathParam("id") final String id, @Context final HttpServletRequest req){
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            IdpConfig idpConfig = new IdpConfig.Builder().id(id).build();

            idpConfigHelper.deleteIdpConfig(idpConfig);

            response = Response.ok(new ResponseEntityView(CollectionsUtils.map("deleted", id))).build();
        } catch (IOException e) {
            Logger.error(this,"Idp is not valid (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error deleting idps", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // deleteIdpConfig.

    @PUT
    @Path("/default/{id}")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public Response setDefault(@PathParam("id") final String id, @Context final HttpServletRequest req){
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            idpConfigHelper.setDefaultIdpConfig(id);

            response = Response.ok(new ResponseEntityView(CollectionsUtils.map("default", id))).build();
        } catch (DotDataException e) {
            Logger.error(this,"Idp not found (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp not found (" + e.getMessage() + ")");
        } catch (IOException e) {
            Logger.error(this,"Idp is not valid (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting setting idp", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // setDefault.

    @GET
    @Path("/default")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public Response getDefault(@Context final HttpServletRequest req){
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            final String defaultIdpConfigId = idpConfigHelper.getDefaultIdpConfigId();

            response =
                Response.ok(new ResponseEntityView(
                    CollectionsUtils.map(IdpConfigWriterReader.DEFAULT_SAML_CONFIG, defaultIdpConfigId))).build();

        } catch (IOException e) {
            Logger.error(this,"Error reading file with Idps (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Idp is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json with Idps (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting default idp", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // getDefault.

    @GET
    @Path("/disabledsites")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public Response getDisabledSites(@Context final HttpServletRequest req){
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            final Map<String, String> disabledSiteIds = idpConfigHelper.getDisabledSiteIds();

            response = Response.ok(new ResponseEntityView(
                CollectionsUtils.map(IdpConfigWriterReader.DISABLE_SAML_SITES, disabledSiteIds)))
                .build();

        } catch (IOException e) {
            Logger.error(this,"Error reading file with disabled sites (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "disable site is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json with Idps (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling disabled site json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting default idp", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // getDisabledSites.

    @POST
    @Path("/disabledsites")
    @JSONP
    @NoCache
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response saveDisabledSited(@Context final HttpServletRequest req,
                                            @FormDataParam("disabledsites") String disabledSites) {
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            HashMap<String, String> disabledSitesMap = new ObjectMapper().readValue(disabledSites, HashMap.class);
            idpConfigHelper.saveDisabledSiteIds(disabledSitesMap);

            response = Response.ok().build();

        } catch (IOException e) {
            Logger.error(this,"Error reading file with disabled sites (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "disable site is not valid (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json with Idps (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling disabled site json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting default idp", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

}

