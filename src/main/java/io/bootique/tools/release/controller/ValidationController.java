package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.validation.ValidatePomService;
import io.bootique.tools.release.view.ValidationView;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Path("/validation")
public class ValidationController extends BaseJobController {

    private final String CONTROLLER_NAME = "validation";

    @Inject
    private DesktopService desktopService;

    @Inject
    private ValidatePomService validatePomService;

    @GET
    public ValidationView home() {
        return new ValidationView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/validate")
    public String validate(@QueryParam("releaseVersion") String releaseVersion,
                           @QueryParam("nextDevVersion") String nextDevVersion,
                           @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repo = project.getRepository();
                if (!mavenService.isMavenProject(repo)) {
                    throw new JobException("NO_POM", "No pom.xml for repo " + repo);
                }
                String[] prepareArgs = {
                        "-DpreparationGoals=clean",
                        "-B", // non-interactive batch mode
                        "release:prepare",
                        "-Darguments=\"-Dgpg.pinentry-mode=default -DskipTests\"",
                        "-Dbootique.version=" + releaseVersion,
                        "-Pgpg", // gpg signing profile
                        "-Ddummy.version=" + releaseVersion,
                        "-Dtag=" + releaseVersion,
                        "-DreleaseVersion=" + releaseVersion,
                        "-DdevelopmentVersion=" + nextDevVersion,
                        "-DdryRun=true"
                };
                desktopService.runMavenCommand(
                        preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), prepareArgs
                );
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
        return "";
    }

    @GET
    @Path("/pom")
    public String validatePom() {
        ObjectContext context = cayenneRuntime.newContext();
        List<Repository> repositories = ObjectSelect.query(Repository.class).select(context);
        List<String> failedRepos = new ArrayList<>();
        for (Repository repository : repositories) {
            if(mavenService.isMavenProject(repository)) {
                failedRepos.addAll(validatePomService.validatePom(repository.getName()));
            }
        }

        if (failedRepos.isEmpty()) {
            return "All poms are valid.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(failedRepos.size()).append(" failed poms: ");
        for (String failedRepo : failedRepos) {
            sb.append("\n").append(failedRepo);
        }
        return sb.toString();
    }

}
