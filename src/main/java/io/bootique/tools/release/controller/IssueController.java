package io.bootique.tools.release.controller;

import io.agrest.DataResponse;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.tools.release.model.persistent.OpenIssue;
import io.bootique.tools.release.view.IssueView;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

@Path("/issue")
public class IssueController extends BaseController {

    @GET
    public IssueView home(@QueryParam("sort") String sort, @QueryParam("filter") String filters, @QueryParam("field") String field) {
        return new IssueView(getCurrentUser(), getCurrentOrganization(), sort, filters, field);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<OpenIssue> showAll(@Context UriInfo uriInfo) {
        return AgJaxrs.select(OpenIssue.class, configuration)
                .clientParams(uriInfo.getQueryParameters()).get();
    }
}
