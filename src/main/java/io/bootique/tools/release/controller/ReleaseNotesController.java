package io.bootique.tools.release.controller;

import io.bootique.tools.release.service.readme.ReleaseNotesService;
import io.bootique.tools.release.view.ReleaseNotesView;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/release-notes")
public class ReleaseNotesController extends BaseController {

    @Inject
    private ReleaseNotesService releaseNotesService;

    @GET
    public ReleaseNotesView home() {
        return new ReleaseNotesView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/generate")
    public String getBranch(@QueryParam("milestoneTitle") String milestoneTitle,
                            @QueryParam("todo") boolean todo) {
        return releaseNotesService.createReleaseNotes(milestoneTitle, todo);
    }
}
