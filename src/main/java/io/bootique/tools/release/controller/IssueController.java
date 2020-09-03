package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.view.IssueView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/issue")
public class IssueController extends BaseController {

    @GET
    public IssueView home(@QueryParam("sort") String sort, @QueryParam("filter") String filters, @QueryParam("field") String field) {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        User user = Ag.select(User.class, configuration).request(agRequest).get().getObjects().get(0);
        return new IssueView(user, organization, sort, filters, field);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<IssueOpen> showAll(@Context UriInfo uriInfo) {
        DataResponse<IssueOpen> issueDataResponse = Ag.select(IssueOpen.class, configuration).uri(uriInfo).get();
        return issueDataResponse;
    }
}
