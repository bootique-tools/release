package io.bootique.tools.release.controller;

import io.agrest.DataResponse;
import io.agrest.jaxrs.AgJaxrs;
import io.bootique.tools.release.model.persistent.PullRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/pr")
public class PRController extends BaseController {

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<PullRequest> showAll(@Context UriInfo uriInfo) {
        return AgJaxrs.select(PullRequest.class, configuration)
                .clientParams(uriInfo.getQueryParameters())
                .get();
    }
}