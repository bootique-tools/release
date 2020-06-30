package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.central.MvnCentralService;
import io.bootique.tools.release.view.ExtraRollbackView;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Path("/extra-rollback")
public class ExtraRollbackController extends BaseReleaseController {

    @Inject
    private MvnCentralService mvnCentralService;

    @GET
    public ExtraRollbackView home() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));
        return new ExtraRollbackView(gitHubApi.getCurrentUser(), organization);
    }

    @POST
    @Path("/create-descriptor")
    public Response createDescriptor(@FormParam("devVersion") String devVersion,
                                     @FormParam("releaseVersion") String releaseVersion,
                                     @FormParam("prevVersion") String prevVersion,
                                     @FormParam("projects") String selected) throws IOException {

        List<Project> selectedProjects = getSelectedProjects(selected);

        prepareRelease(
                createDescriptor(
                        prevVersion,
                        releaseVersion,
                        devVersion,
                        selectedProjects,
                        ReleaseStage.NO_RELEASE,
                        RollbackStage.ROLLBACK_BINTRAY,
                        false));

        return validate(releaseVersion, selectedProjects) ?
                Response.seeOther(URI.create("extra-rollback/rollback-fail")).build() :
                Response.seeOther(URI.create("rollback/next")).build();
    }

    @GET
    @Path("/rollback-fail")
    public ExtraRollbackView rollbackFail() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        return new ExtraRollbackView(gitHubApi.getCurrentUser(), organization, "This version was synced with mvn central. Rollback is not available.");
    }

    @Override
    boolean validate(String releaseVersion, List<Project> selectedProjects) {
        return mvnCentralService.isSync(releaseVersion, selectedProjects);
    }
}
