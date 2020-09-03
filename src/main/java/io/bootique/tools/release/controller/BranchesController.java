package io.bootique.tools.release.controller;

import io.agrest.*;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.BranchesView;
import org.apache.cayenne.ObjectContext;

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
    public BranchesView home(@Context UriInfo uriInfo) {
        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        User user = Ag.select(User.class, configuration).uri(uriInfo).get().getObjects().get(0);
        return new BranchesView(user, organization);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll(@Context UriInfo uriInfo) {

        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\",\"rootModule\"]")
                .build();

        return getProjects(project -> true, agRequest);
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

                AgRequest agRequest = Ag.request(configuration).build();
                ObjectContext objectContext = repository.getObjectContext();
                DataResponse<Project> projectDataResponse = getProjects(p -> true, agRequest);
                objectContext.deleteObjects(projectDataResponse.getObjects());
                objectContext.commitChanges();

                gitService.checkoutBranch(repository, title);

                projectDataResponse = getProjects(p -> true, agRequest);

                for (Project p : projectDataResponse.getObjects()) {
                    if (p.getRepository().getName().equals(repository.getName())) {
                        project.setRootModule(
                                new Module(p.getRootModule().getGroupStr(), p.getRootModule().getGithubId(), p.getRootModule().getVersion()));
                        project.setBranchName(title);
                        break;
                    }
                }
                return "";
            } catch (DesktopException ex) {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

}
