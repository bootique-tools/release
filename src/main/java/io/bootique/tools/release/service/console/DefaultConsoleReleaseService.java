package io.bootique.tools.release.service.console;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.release.ReleaseService;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

public class DefaultConsoleReleaseService implements ConsoleReleaseService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultConsoleReleaseService.class);

    @Inject
    private Map<ReleaseStage, Function<Repository, String>> releaseMap;

    @Inject
    ReleaseService releaseService;

    @Inject
    GitHubApi gitHubApi;

    @Inject
    MavenService mavenService;

    @Inject
    private GitService gitService;

    @Inject
    private LoggerService loggerService;

    @Override
    public boolean checkReadyForRelease(String fromVersion, String releaseVersion, String devVersion, List<String> excludeModules) {
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Project> projects = mavenService.getProjects(organization,
                project -> project.getVersion().equals(fromVersion) && !excludeModules.contains(project.getRepository().getName()));
        if (projects.isEmpty()) {
            LOGGER.info("There are no projects found for version: " + fromVersion + ".");
            return false;
        }

        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor(fromVersion,
                releaseVersion, devVersion, projects, ReleaseStage.RELEASE_PULL, RollbackStage.NO_ROLLBACK, false);
        releaseService.createReleaseDescriptor(releaseDescriptor);
        return true;
    }

    @Override
    public void startReleaseFromConsole() {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        loggerService.prepareLogger(releaseService.getReleaseDescriptor());

        for (ReleaseStage releaseStage : ReleaseStage.values()) {
            if (releaseStage == ReleaseStage.NO_RELEASE) {
                continue;
            }
            LOGGER.info("Start " + releaseStage + ".");
            releaseDescriptor.setCurrentReleaseStage(releaseStage);
            for (Project project : releaseDescriptor.getProjectList()) {
                try {
                    releaseMap.get(releaseStage).apply(project.getRepository());
                    LOGGER.info("Current stage: " + releaseStage + ". " + project.getRepository().getName() + " - done.");
                } catch (Exception ex) {
                    throw new DesktopException(ex.getMessage(), ex);
                }
            }
        }
    }
}
