package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.view.IssueView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.function.Predicate;

@Path("/issue")
public class IssueController extends BaseController {

    @Inject
    private ContentService contentService;

    private static final Predicate<Issue> DEFAULT_FILTER = issue -> true;

    private static final Comparator<Issue> DEFAULT_COMPARATOR = Comparator.comparing(Issue::getCreatedAt).reversed();

    @GET
    public IssueView home(@QueryParam("sort") String sort, @QueryParam("filter") String filters, @QueryParam("field") String field) {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        return new IssueView(gitHubApi.getCurrentUser(), organization, sort, filters, field);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Issue> showAll(@Context UriInfo uriInfo) {
        DataResponse<Issue> issueDataResponse = Ag.select(Issue.class, configuration).uri(uriInfo).get();
        return issueDataResponse;
    }
}
