package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.MavenView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("maven")
public class MavenController extends BaseController {

    private static final String CONTROLLER_NAME = "maven";

    @Inject
    private BatchJobService jobService;

    @Inject
    private DesktopService desktopService;

    @GET
    public MavenView home() {
        return new MavenView(getCurrentUser(), getCurrentOrganization());
    }

    @POST
    @Path("verify")
    public void verifyAll() {
        AgRequest agRequest = Ag.request(configuration).build();
        DataResponse<Project> projects = getProjects(project -> true, agRequest);
        List<Repository> repositories = projects.getObjects().stream()
                .map(Project::getRepository)
                .collect(Collectors.toList());

        if (jobService.getCurrentJob() != null && !jobService.getCurrentJob().isDone()) {
            return;
        }

        Function<Repository, String> repoProcessor = repo -> {
            if (!mavenService.isMavenProject(repo)) {
                return "NO_POM"; // throw new JobException("NO_POM", "No pom.xml for repo " + repo);
            }
            try {
                desktopService.runMavenCommand(
                        preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()),
                        "clean", "install", "-B", "-q"
                );
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };

        BatchJobDescriptor<Repository, String> descriptor = BatchJobDescriptor.<Repository, String>builder()
                .data(repositories).processor(repoProcessor).controllerName(CONTROLLER_NAME).build();
        preferences.set(BatchJobService.CURRENT_JOB_ID, jobService.submit(descriptor).getId());
    }
}