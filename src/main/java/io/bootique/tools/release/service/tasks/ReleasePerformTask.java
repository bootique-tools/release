package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.function.Function;

public class ReleasePerformTask implements Function<Repository, String>  {

    @Inject
    private LoggerService logger;

    @Inject
    private MavenService mavenService;

    @Inject
    private DesktopService desktopService;

    @Inject
    private PreferenceService preferences;

    @Override
    public String apply(Repository repo) {

        logger.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_PERFORM));

        if (!mavenService.isMavenProject(repo)) {
            throw new JobException("NO_POM", "No pom.xml for repo " + repo);
        }

        Path repoPath = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName());
        String[] performArgs = {
                "-B", // non-interactive batch mode
                "release:perform",
                "-P", "gpg",
                "-DskipTests",
                "-Dgpg.pinentry-mode=default",
                "-Darguments=\"-Dgpg.pinentry-mode=default\"",
                // "-DdryRun=true"
        };

        try {
            return desktopService.runMavenCommand(repoPath, performArgs);
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
