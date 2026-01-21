package io.bootique.tools.release.controller;

import io.agrest.DataResponse;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.tools.release.model.persistent.PullRequest;
import io.bootique.tools.release.view.PullRequestView;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/pr")
public class PRController extends BaseController {

    @GET
    public PullRequestView home(@QueryParam("sort") String sort, @QueryParam("filter") String filters, @QueryParam("field") String field) {
        return new PullRequestView(getCurrentUser(), getCurrentOrganization(), sort, filters, field);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<PullRequest> showAll(@Context UriInfo uriInfo) {
        return AgJaxrs.select(PullRequest.class, configuration)
                .clientParams(uriInfo.getQueryParameters())
                .get();
    }
}