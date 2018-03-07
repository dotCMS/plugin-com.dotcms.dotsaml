package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotcms.repackage.javax.ws.rs.GET;
import com.dotcms.repackage.javax.ws.rs.Path;
import com.dotcms.repackage.javax.ws.rs.Produces;
import com.dotcms.repackage.javax.ws.rs.core.Context;
import com.dotcms.repackage.javax.ws.rs.core.MediaType;
import com.dotcms.repackage.javax.ws.rs.core.Response;
import com.dotcms.repackage.org.glassfish.jersey.server.JSONP;
import com.dotcms.rest.ResponseEntityView;
import com.dotcms.rest.annotation.InitRequestRequired;
import com.dotcms.rest.annotation.NoCache;
import com.dotcms.rest.exception.mapper.ExceptionMapperUtil;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@Path("/v1/saml")
public class SamlResource implements Serializable {

    private final IdpConfigHelper idpConfigHelper;

    public SamlResource() {
        this.idpConfigHelper = IdpConfigHelper.getInstance();
    }

    @GET
    @Path("/idps")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response getRecentBaseTypes(@Context final HttpServletRequest request) {

        Response response;

        try {
            List<IdpConfig> idps = this.idpConfigHelper.getIdpConfigs(request);
            response = Response.ok(new ResponseEntityView(idps)).build();
        } catch (Exception e) { // this is an unknown error, so we report as a 500.

            response = ExceptionMapperUtil.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    } // getTypes.

}

