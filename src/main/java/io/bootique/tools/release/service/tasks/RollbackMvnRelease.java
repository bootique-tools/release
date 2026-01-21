package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import jakarta.inject.Inject;

public class RollbackMvnRelease implements ReleaseTask {

    @Inject
    LoggerService loggerService;

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Override
    public String apply(Repository repo) {
        try {
            loggerService.setAppender(repo.getName(), "rollback", String.valueOf(RollbackStage.ROLLBACK_MVN));
            desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "release:rollback"
            );
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
