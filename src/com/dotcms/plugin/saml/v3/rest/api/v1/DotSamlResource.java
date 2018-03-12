package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotcms.repackage.javax.ws.rs.Consumes;
import com.dotcms.repackage.javax.ws.rs.DELETE;
import com.dotcms.repackage.javax.ws.rs.GET;
import com.dotcms.repackage.javax.ws.rs.POST;
import com.dotcms.repackage.javax.ws.rs.Path;
import com.dotcms.repackage.javax.ws.rs.PathParam;
import com.dotcms.repackage.javax.ws.rs.Produces;
import com.dotcms.repackage.javax.ws.rs.core.Context;
import com.dotcms.repackage.javax.ws.rs.core.MediaType;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.repackage.org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import com.dotcms.repackage.org.glassfish.jersey.media.multipart.FormDataParam;
import com.dotcms.repackage.org.glassfish.jersey.server.JSONP;
import com.dotcms.rest.ResponseEntityView;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.annotation.NoCache;
import com.dotcms.rest.exception.mapper.ExceptionMapperUtil;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@Path("/v1/dotsaml")
public class DotSamlResource implements Serializable {

    private final IdpConfigHelper idpConfigHelper;
    private final WebResource webResource;

    public DotSamlResource() {
        this.idpConfigHelper = IdpConfigHelper.getInstance();
        this.webResource = new WebResource();
    }

    @GET
    @Path("/idps")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response getIdps(@Context final HttpServletRequest request) {
        this.webResource.init(null, true, request, true, null);

        Response response;

        try {
            List<IdpConfig> idps = this.idpConfigHelper.getIdpConfigs();
            response = Response.ok(new ResponseEntityView(idps)).build();
        } catch (IOException e) {
            Logger.error(this,"Error handling file (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling file (" + e.getMessage() + ")");
        } catch (JSONException e) {
            Logger.error(this,"Error handling json (" + e.getMessage() + ")", e);
            response = ExceptionMapperUtil.createResponse(null, "Error handling json (" + e.getMessage() + ")");
        } catch (Exception e) { // this is an unknown error, so we report as a 500.
            Logger.error(this,"Error getting idps", e);
            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // getIdps.

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
                                          @FormDataParam("publicCert") InputStream publicCert,
                                          @FormDataParam("publicCert") FormDataContentDisposition publicCertFileDetail,
                                          @FormDataParam("idPMetadataFile") InputStream idPMetadataFile,
                                          @FormDataParam("idPMetadataFile") FormDataContentDisposition idPMetadataFileDetail,
                                          @FormDataParam("optionalProperties") String optionalProperties) {
        this.webResource.init(null, true, req, true, null);

        Response response;

        try {
            IdpConfig idpConfig = new IdpConfig();
            idpConfig.setId(id);
            idpConfig.setIdpName(idpName);
            idpConfig.setEnabled(enabled);
            idpConfig.setsPIssuerURL(sPIssuerURL);
            idpConfig.setsPEndponintHostname(sPEndponintHostname);
            idpConfig.setOptionalProperties(optionalProperties);

            /*File privateKey = File.createTempFile("private", "key");
            FileUtils.copyInputStreamToFile(privateKeyStream, privateKey);
            idpConfig.setPrivateKey(privateKey);*/

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
            IdpConfig idpConfig = new IdpConfig();
            idpConfig.setId(id);

            idpConfigHelper.deleteIdpConfig(idpConfig);

            JSONObject joe = new JSONObject();
            joe.put("deleted", idpConfig.getId());

            response = Response.ok(new ResponseEntityView(joe.toString())).build();
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
    }

}

