package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.view.ReleaseRollbackView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/rollback")
public class ReleaseRollbackController extends BaseController {

    @Inject
    private ReleaseService releaseService;

    @GET
    @Path("/start")
    public Response start() throws URISyntaxException {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        releaseService.prepareRelease();
        releaseDescriptor.setCurrentReleaseStage(ReleaseStage.NO_RELEASE);
        releaseDescriptor.setCurrentRollbackStage(RollbackStage.ROLLBACK_BINTRAY);
        return Response.seeOther(new URI("rollback/next")).build();
    }

    @GET
    @Path("/next")
    public Response next() throws URISyntaxException {
        releaseService.nextRollbackStage();
        return Response.seeOther(new URI("rollback/current-step")).build();
    }

    @GET
    @Path("/current-step")
    public ReleaseRollbackView currentStep() {

        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        return new ReleaseRollbackView(gitHubApi.getCurrentUser(),
                organization, releaseService.getReleaseDescriptor().getCurrentRollbackStage());
    }

}
