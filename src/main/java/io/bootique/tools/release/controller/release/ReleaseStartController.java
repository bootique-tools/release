package io.bootique.tools.release.controller.release;

import io.bootique.tools.release.controller.BaseController;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseVersions;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.view.BaseView;
import io.bootique.tools.release.view.ReleaseProcessView;
import io.bootique.tools.release.view.ReleaseView;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Path("/release")
public class ReleaseStartController extends BaseController {

    @Inject
    public ReleaseDescriptorService releaseDescriptorService;

    @Inject
    public LoggerService loggerService;

    @GET
    public BaseView getReleaseView() {

        if (releaseDescriptorService.getReleaseDescriptor() == null) {
            return new ReleaseView(getCurrentUser(), getCurrentOrganization());
        }

        loggerService.prepareLogger(releaseDescriptorService.getReleaseDescriptor());
        return new ReleaseProcessView(getCurrentUser(), getCurrentOrganization());
    }

    @POST
    @Path("/create-descriptor")
    public Response createDescriptor(@FormParam("fromVersion") String fromVersion,
                                     @FormParam("releaseVersion") String releaseVersion,
                                     @FormParam("devVersion") String devVersion,
                                     @FormParam("projects") String selected) throws IOException {

        List<Project> selectedProjects = getSelectedProjects(selected);
        ReleaseVersions versions = new ReleaseVersions(releaseVersion, devVersion, fromVersion);

        releaseDescriptorService.createReleaseDescriptor(versions, selectedProjects);

        return Response.seeOther(URI.create("release/")).build();
    }
}
