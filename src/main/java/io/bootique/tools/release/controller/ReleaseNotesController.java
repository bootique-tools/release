package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;
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
        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        User user = Ag.select(User.class, configuration).uri(uriInfo).get().getObjects().get(0);
        return new ReleaseNotesView(user, organization);
    }

    @GET
    @Path("/generate")
    public String getBranch(@QueryParam("milestoneTitle") String milestoneTitle) {
        return releaseNotesService.createReleaseNotes(milestoneTitle);
    }
}
