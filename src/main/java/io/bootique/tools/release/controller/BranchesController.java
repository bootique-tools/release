package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.BranchesView;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Path("branches")
public class BranchesController extends DefaultBaseController{

    private final String CONTROLLER_NAME = "branches";

    @GET
    public BranchesView home() {
        return new BranchesView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization());
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> showAll() {
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Project> projects = haveMissingRepos(organization) ? new ArrayList<>() :
                mavenService.getProjects(organization, project -> true);
        projects.forEach(project -> project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName())));
        return projects;
    }

    @GET
    @Path("/get-branch")
    public String getBranch(@QueryParam("name") String name) {
        return gitService.getCurrentBranchName(name);
    }

    @GET
    @Path("/createBranch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createBranch(@QueryParam("branchTitle") String title,
                       @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                if(gitService.getStatus(repository.getName())) {
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
    @Produces(MediaType.APPLICATION_JSON)
    public void checkoutBranch(@QueryParam("branchTitle") String title,
                             @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                gitService.checkoutBranch(repository, title);
                project.setBranchName(title);
                return "";
            } catch (DesktopException ex) {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

}
