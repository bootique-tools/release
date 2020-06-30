package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.BranchesView;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.function.Function;

@Path("branches")
public class BranchesController extends DefaultBaseController {

    private final String CONTROLLER_NAME = "branches";

    @GET
    public BranchesView home() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));
        return new BranchesView(gitHubApi.getCurrentUser(), organization);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll(@Context UriInfo uriInfo) {

        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\",\"rootModule\"]")
                .build();

        return getProjects(agRequest, project -> true);
    }

    @GET
    @Path("/get-branch")
    public String getBranch(@QueryParam("name") String name) {
        return gitService.getCurrentBranchName(name);
    }

    @GET
    @Path("/createBranch")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createBranch(@QueryParam("branchTitle") String title,
                             @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                if (gitService.getStatus(repository.getName())) {
                    project.setBranchName(title);
                    gitService.createBranch(repository, title);
                } else {
                    throw new DesktopException("You have uncommited changes.");
                }
                return "";
            } catch (DesktopException ex) {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

    @GET
    @Path("/checkoutBranch")
    @Consumes(MediaType.APPLICATION_JSON)
    public void checkoutBranch(@QueryParam("branchTitle") String title,
                               @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                gitService.checkoutBranch(repository, title);
                project.setBranchName(title);
                repository.getObjectContext().commitChanges();
                return "";
            } catch (DesktopException ex) {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

}
