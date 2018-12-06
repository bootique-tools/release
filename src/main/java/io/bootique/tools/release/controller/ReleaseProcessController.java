package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.view.BaseView;
import io.bootique.tools.release.view.ReleaseProcessView;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Path("/release")
public class ReleaseProcessController extends BaseReleaseController {

    @POST
    @Path("/create-descriptor")
    public Response createDescriptor(@FormParam("fromVersion") String version,
                                             @FormParam("releaseVersion") String releaseVersion,
                                             @FormParam("devVersion") String devVersion,
                                             @FormParam("projects") String selected,
                                             @FormParam("mode") boolean mode) throws IOException {

        List<Project> selectedProjects = getSelectedProjects(selected);
        prepareRelease(
                createDescriptor(
                        version,
                        releaseVersion,
                        devVersion,
                        selectedProjects,
                        ReleaseStage.RELEASE_PULL,
                        RollbackStage.NO_ROLLBACK,
                        mode));

        return validate(releaseVersion, selectedProjects) ?
                Response.serverError().build() :
                Response.seeOther(URI.create("release/next")).build();
    }

    @GET
    @Path("/current-step")
    public BaseView currentStep() {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        return new ReleaseProcessView(gitHubApi.getCurrentUser(),
                gitHubApi.getCurrentOrganization(), releaseDescriptor.getCurrentReleaseStage(), releaseDescriptor.isAutoReleaseMode());
    }

    @GET
    @Path("/next")
    public Response next() {
        releaseService.nextReleaseStage();
        return Response.seeOther(URI.create("release/current-step")).build();
    }

    @Override
    boolean validate(String releaseVersion, List<Project> selectedProjects) {
        return false;
    }
}
