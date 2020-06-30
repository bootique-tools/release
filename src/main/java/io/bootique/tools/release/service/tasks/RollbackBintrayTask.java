package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.bintray.BintrayApi;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;
import javax.inject.Inject;

public class RollbackBintrayTask implements Function<Repository, String> {

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseService releaseService;

    @Inject
    private BintrayApi bintrayApi;

    @Override
    public String apply(Repository repo) {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        try {
            loggerService.setAppender(repo.getName(), "rollback", String.valueOf(RollbackStage.ROLLBACK_BINTRAY));
            bintrayApi.getAndDeleteVersion(repo, releaseDescriptor.getReleaseVersion());
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
