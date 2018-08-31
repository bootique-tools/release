package io.bootique.tools.release.service.tasks;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.bintray.BintrayApi;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;

public class ReleaseSyncTask implements Function<Repository, String> {

    @Inject
    private LoggerService loggerService;

    @Inject
    private BintrayApi bintrayApi;

    @Inject
    private ReleaseService releaseService;

    @Override
    public String apply(Repository repo) {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        try {
            loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_SYNC));
            bintrayApi.publishUploadedContent(repo, releaseDescriptor.getReleaseVersion());
            bintrayApi.syncWithCentral(repo, releaseDescriptor.getReleaseVersion());
            releaseService.saveRelease();
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
