package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.view.MavenView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("maven")
public class MavenController extends BaseController {

    @Inject
    private MavenService mavenService;

    @Inject
    private BatchJobService jobService;

    @Inject
    private DesktopService desktopService;

    @GET
    public MavenView home() {
        return new MavenView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization());
    }

    @GET
    @Path("verify")
    public void verifyAll() {
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Project> projects = mavenService.getProjects(organization, p -> true);
        List<Repository> repositories = projects.stream()
                .map(Project::getRepository)
                .collect(Collectors.toList());

        if(jobService.getCurrentJob() != null && !jobService.getCurrentJob().isDone()){
            return;
        }

        Function<Repository, String> repoProcessor = repo -> {
            if(!mavenService.isMavenProject(repo)) {
                return "NO_POM"; // throw new JobException("NO_POM", "No pom.xml for repo " + repo);
            }
            try {
                desktopService.runMavenCommand(
                        preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "clean", "install", "-B", "-q"
                );
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };

        BatchJobDescriptor<Repository, String> descriptor = new BatchJobDescriptor<>(repositories, repoProcessor);
        preferences.set(BatchJobService.CURRENT_JOB_ID, jobService.submit(descriptor).getId());
    }
}
