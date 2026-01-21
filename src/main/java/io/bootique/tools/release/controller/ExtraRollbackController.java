package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.central.MvnCentralService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.view.ExtraRollbackView;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * @deprecated need to be refactoring for new model of project. After removing module entity (and rootModule from project),
 * the is no gitHubId property for query.
 */
@Deprecated
@Path("/extra-rollback")
public class ExtraRollbackController extends BaseController {

    @Inject
    private MvnCentralService mvnCentralService;

    @Inject
    private ReleaseDescriptorService releaseDescriptorService;

    @GET
    public ExtraRollbackView home() {
       return new ExtraRollbackView(getCurrentUser(), getCurrentOrganization());

    }

    @POST
    @Path("/create-descriptor")
    public Response createDescriptor(@FormParam("releaseVersion") String releaseVersion,
                                     @FormParam("projects") String selected) throws IOException {

        List<Project> selectedProjects = getSelectedProjects(selected);
        releaseDescriptorService.getReleaseDescriptor();

        return validate(releaseVersion, selectedProjects) ?
                Response.seeOther(URI.create("extra-rollback/rollback-fail")).build() :
                Response.seeOther(URI.create("rollback/next")).build();
    }

    @GET
    @Path("/rollback-fail")
    public ExtraRollbackView rollbackFail() {
        return new ExtraRollbackView(getCurrentUser(), getCurrentOrganization(), "This version was synced with mvn central. Rollback is not available.");
    }


    boolean validate(String releaseVersion, List<Project> selectedProjects) {
        return mvnCentralService.isSync(releaseVersion, selectedProjects);
    }
}
