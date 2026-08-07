package io.bootique.tools.release.controller.release;

import io.bootique.tools.release.controller.BaseController;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseSummary;
import io.bootique.tools.release.model.release.ReleaseVersions;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/release")
public class ReleaseStartController extends BaseController {

    @Inject
    public ReleaseDescriptorService releaseDescriptorService;

    @Inject
    public LoggerService loggerService;

    /**
     * Whether a release descriptor exists, and the versions it targets. Lets a client decide between the
     * project-selection page and the release board.
     */
    @GET
    @Path("/summary")
    @Produces(MediaType.APPLICATION_JSON)
    public ReleaseSummary getSummary() {

        ReleaseDescriptor descriptor = releaseDescriptorService.getReleaseDescriptor();
        if (descriptor == null) {
            return ReleaseSummary.NONE;
        }

        // the appenders backing the per-stage logs are set up lazily on first look at an active release
        loggerService.prepareLogger(descriptor);
        return ReleaseSummary.of(descriptor.getReleaseVersions());
    }

    @POST
    @Path("/create-descriptor")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReleaseSummary createDescriptor(CreateReleaseRequest request) {

        List<Project> selectedProjects = getSelectedProjects(request.projects());
        ReleaseVersions versions = new ReleaseVersions(
                request.releaseVersion(), request.devVersion(), request.fromVersion());

        releaseDescriptorService.createReleaseDescriptor(versions, selectedProjects);

        return getSummary();
    }

    /**
     * @param projects repository names of the modules to release, in the order they should be released
     */
    public record CreateReleaseRequest(String fromVersion, String releaseVersion, String devVersion,
                                       List<String> projects) {
    }
}
