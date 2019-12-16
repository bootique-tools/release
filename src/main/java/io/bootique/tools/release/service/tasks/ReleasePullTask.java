package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;
import javax.inject.Inject;

public class ReleasePullTask implements Function<Repository, String> {

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseService releaseService;

    @Inject
    private GitService gitService;

    @Override
    public String apply(Repository repo) {
        try {
            loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_PULL));
            gitService.update(repo);
            releaseService.saveRelease(repo);
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }

}
