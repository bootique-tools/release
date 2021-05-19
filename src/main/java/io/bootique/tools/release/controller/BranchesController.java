package io.bootique.tools.release.controller;

import io.agrest.DataResponse;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.BranchesView;
import org.apache.cayenne.query.ObjectSelect;

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
public class BranchesController extends BaseJobController {

    private final String CONTROLLER_NAME = "branches";

    @GET
    public BranchesView home(@Context UriInfo uriInfo) {
        return new BranchesView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll(@Context UriInfo uriInfo) {
        return fetchProjects("[\"repository\",\"rootModule\"]");
    }

    @GET
    @Path("/get-branch")
    public String getBranch(@QueryParam("name") String name) {
        Repository repository = ObjectSelect.query(Repository.class, Repository.NAME.eq(name))
                .selectOne(cayenneRuntime.newContext());
        return gitService.getCurrentBranchName(repository);
    }

    @GET
    @Path("/createBranch")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createBranch(@QueryParam("branchTitle") String branch,
                               @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            Repository repository = project.getRepository();
            try {
                if (gitService.isClean(repository)) {
                    gitService.createBranch(repository, branch);
                    project.setBranchName(branch);
                    project.getObjectContext().commitChanges();
                    return "";
                } else {
                    throw new DesktopException("You have uncommitted changes in the '" + repository.getName() + "' repository.");
                }
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
        return "OK";
    }

    @GET
    @Path("/checkoutBranch")
    @Consumes(MediaType.APPLICATION_JSON)
    public String checkoutBranch(@QueryParam("branchTitle") String branch,
                                 @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                gitService.checkoutBranch(project.getRepository(), branch);
                project.setBranchName(branch);
                project.getObjectContext().commitChanges();
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
        return "OK";
    }

}
