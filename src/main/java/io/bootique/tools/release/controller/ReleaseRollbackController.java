package io.bootique.tools.release.controller;

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
        releaseDescriptor.setCurrentRollbackStage(RollbackStage.ROLLBACK_SONATYPE);
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
        return new ReleaseRollbackView(
                getCurrentUser(),
                getCurrentOrganization(),
                releaseService.getReleaseDescriptor().getCurrentRollbackStage()
        );
    }

}
