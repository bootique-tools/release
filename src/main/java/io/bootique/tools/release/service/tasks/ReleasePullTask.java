package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;

import javax.inject.Inject;

public class ReleasePullTask implements ReleaseTask {

    @Inject
    protected LoggerService logger;

    @Inject
    protected GitService gitService;

    @Override
    public String apply(Repository repo) {
        try {
            logger.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_PULL));
            gitService.update(repo);
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }

}
