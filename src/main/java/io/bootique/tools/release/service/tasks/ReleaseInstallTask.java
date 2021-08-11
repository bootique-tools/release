package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import javax.inject.Inject;
import java.util.function.Function;

public class ReleaseInstallTask implements Function<Repository, String> {

    @Inject
    LoggerService logger;

    @Inject
    PreferenceService preferences;

    @Inject
    DesktopService desktopService;

    @Override
    public String apply(Repository repo) {
        try {
            logger.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_INSTALL));
            return desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "clean", "install", "-B", "-DskipTests");
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
