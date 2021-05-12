package io.bootique.tools.release.controller;

import io.bootique.tools.release.service.readme.ReleaseNotesService;
import io.bootique.tools.release.view.ReleaseNotesView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/release-notes")
public class ReleaseNotesController extends BaseController {

    @Inject
    private ReleaseNotesService releaseNotesService;

    @GET
    public ReleaseNotesView home(@Context UriInfo uriInfo) {
        return new ReleaseNotesView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/generate")
    public String getBranch(@QueryParam("milestoneTitle") String milestoneTitle) {
        return releaseNotesService.createReleaseNotes(milestoneTitle);
    }
}
