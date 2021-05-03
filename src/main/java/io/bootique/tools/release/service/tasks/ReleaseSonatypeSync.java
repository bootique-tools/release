package io.bootique.tools.release.service.tasks;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.ReleaseService;

public class ReleaseSonatypeSync implements Function<Repository, String> {

    @Inject
    LoggerService loggerService;

    @Inject
    MavenService mavenService;

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Inject
    ReleaseService releaseService;

    @Override
    public String apply(Repository repo) {
        loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_SYNC));
        if (!mavenService.isMavenProject(repo)) {
            throw new JobException("NO_POM", "No pom.xml for repo " + repo);
        }

        Path repoPath = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName());
        String[] args = {
                "-B", // non-interactive batch mode
                "nexus-staging:rc-list",
                "-DserverId=local-nexus"
        };

        String result;
        try {
            result = desktopService.runMavenCommand(repoPath, args);
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }

        result = result.lines()
                .dropWhile(s -> !s.startsWith("[INFO] Getting list of available staging repositories..."))
                .takeWhile()
                .collect(Collectors.joining("\n"));

        releaseService.saveRelease(repo);

        return result;
    }
}
