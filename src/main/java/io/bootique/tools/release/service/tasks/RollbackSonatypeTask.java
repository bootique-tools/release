package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class RollbackSonatypeTask implements Function<Repository, String> {

    private static final Path CHECKOUT_PATH = Paths.get("target/checkout/");

    @Inject
    LoggerService logger;

    @Inject
    MavenService mavenService;

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Override
    public String apply(Repository repo) {
        logger.setAppender(repo.getName(), "rollback", String.valueOf(RollbackStage.ROLLBACK_SONATYPE));
        if (!mavenService.isMavenProject(repo)) {
            throw new JobException("NO_POM", "No pom.xml for repo " + repo);
        }

        Path repoPath = preferences.get(GitService.BASE_PATH_PREFERENCE)
                .resolve(repo.getName()).resolve(CHECKOUT_PATH);
        String[] args = {
                "-B", // non-interactive batch mode
                "nexus-staging:drop"
        };

        try {
            return desktopService.runMavenCommand(repoPath, args);
        } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
        }
    }
}
