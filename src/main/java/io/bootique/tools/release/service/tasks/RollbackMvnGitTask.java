package io.bootique.tools.release.service.tasks;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;

public class RollbackMvnGitTask implements Function<Repository, String>{

    @Inject
    private LoggerService loggerService;

    @Inject
    private GitService gitService;

    @Inject
    private DesktopService desktopService;

    @Inject
    private PreferenceService preferences;

    @Inject
    private ReleaseService releaseService;

    @Override
    public String apply(Repository repo) {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        try {
            loggerService.setAppender(repo.getName(), "rollback", String.valueOf(RollbackStage.ROLLBACK_MVN));
            releaseService.rollbackPom(repo.getName(), releaseDescriptor);
            desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "release:clean"
            );
            gitService.addAndCommit(repo);
            gitService.deleteTag(repo, releaseDescriptor.getReleaseVersion());
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
