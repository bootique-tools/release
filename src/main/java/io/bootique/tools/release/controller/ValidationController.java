package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.validation.ValidatePomService;
import io.bootique.tools.release.view.ValidationView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Path("/validation")
public class ValidationController extends DefaultBaseController {

    private final String CONTROLLER_NAME = "validation";

    @Inject
    private DesktopService desktopService;

    @Inject
    private ValidatePomService validatePomService;

    @GET
    public ValidationView home() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        User user = Ag.select(User.class, configuration).request(agRequest).get().getObjects().get(0);
        return new ValidationView(user, organization);
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
        List<Project> allProjects = getAllProjects();
        List<String> failedRepos = new ArrayList<>();
        for (Project project : allProjects) {
            Repository repository = project.getRepository();
            String repoName = repository.getName();
            failedRepos.addAll(validatePomService.validatePom(repoName));
        }
        if (failedRepos.isEmpty()) {
            return "All poms are valid.";
        }

        return "Failed poms: " + String.join(",", failedRepos);
    }

}
