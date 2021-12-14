package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Function;

import static java.awt.SystemColor.text;

public class ReleasePullTask implements Function<Repository, String> {

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
