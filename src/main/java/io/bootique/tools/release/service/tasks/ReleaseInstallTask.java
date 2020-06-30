package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;
import javax.inject.Inject;

public class ReleaseInstallTask implements Function<Repository, String> {

    @Inject
    private LoggerService loggerService;

    @Inject
    private DesktopService desktopService;

    @Inject
    private ReleaseService releaseService;

    @Inject
    private PreferenceService preferences;

    @Override
    public String apply(Repository repo) {
        try {
            loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_INSTALL));
            desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "clean", "install", "-B");
            releaseService.saveRelease(repo);
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
