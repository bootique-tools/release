package io.bootique.tools.release.service.tasks;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.bintray.BintrayApi;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;

public class ReleaseBintrayTask implements Function<Repository, String> {

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseService releaseService;

    @Inject
    private BintrayApi bintrayApi;

    @Override
    public String apply(Repository repo) {
        try {
            loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_BINTRAY_CHECK));
            if(!bintrayApi.getRepository(repo)) {
                throw new DesktopException("Bintray repo missed.");
            }
            releaseService.saveRelease();
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
