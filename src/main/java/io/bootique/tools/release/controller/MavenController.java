package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.MavenView;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.cayenne.query.ObjectSelect;

import java.util.List;
import java.util.function.Function;

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
        if (jobService.getCurrentJob() != null && !jobService.getCurrentJob().isDone()) {
            return;
        }

        List<Project> projects = mavenService
                .sortProjects(ObjectSelect.query(Project.class).select(cayenneRuntime.newContext()));

        BatchJobDescriptor<Project, String> descriptor = BatchJobDescriptor.<Project, String>builder()
                .data(projects)
                .processor(new MavenBuildProcessor())
                .controllerName(CONTROLLER_NAME)
                .build();
        preferences.set(BatchJobService.CURRENT_JOB_ID, jobService.submit(descriptor).getId());
    }

    private class MavenBuildProcessor implements Function<Project, String> {
        @Override
        public String apply(Project project) {
            Repository repository = project.getRepository();
            if (!mavenService.isMavenProject(repository)) {
                return "NO_POM"; // throw new JobException("NO_POM", "No pom.xml for repo " + repo);
            }
            try {
                desktopService.runMavenCommand(
                        preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repository.getName()),
                        "clean", "install", "-B", "-q"
                );
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        }
    }
}