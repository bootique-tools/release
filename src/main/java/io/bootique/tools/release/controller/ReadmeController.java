package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.readme.CreateReadmeService;
import io.bootique.tools.release.view.ReadmeView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/readme")
public class ReadmeController extends BaseController {

    @Inject
    private CreateReadmeService createReadmeService;

    @Inject
    private ContentService contentService;

    @GET
    public ReadmeView home(@Context UriInfo uriInfo) {
        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        return new ReadmeView(gitHubApi.getCurrentUser(), organization);
    }

    @GET
    @Path("/generate")
    public StringBuilder getBranch(@Context UriInfo uriInfo, @QueryParam("milestoneTitle") String milestoneTitle) {
        return createReadme(uriInfo, milestoneTitle);
    }

    private StringBuilder createReadme(UriInfo uriInfo, String milestoneTitle) {
        DataResponse<Repository> dataResponse = Ag.select(Repository.class, configuration).uri(uriInfo).get();
        return createReadmeService.createReadme(dataResponse.getObjects(), milestoneTitle);
    }
}
