package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.function.Function;
import javax.inject.Inject;

public class ReleasePreparePerformTask implements Function<Repository, String> {

    @Inject
    private LoggerService loggerService;

    @Inject
    private MavenService mavenService;

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
            loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_PREPARE_PERFORM));
            if (!mavenService.isMavenProject(repo)) {
                throw new JobException("NO_POM", "No pom.xml for repo " + repo);
            }
            String[] prepareArgs = {
                    "-DpreparationGoals=clean",
                    "-B", // non-interactive batch mode
                    "release:prepare",
                    "-P", "gpg",
                    "-DskipTests",
                    "-Dgpg.pinentry-mode=default",
                    "-Darguments=\"-Dgpg.pinentry-mode=default\"",
                    "-Dbootique.version=" + releaseDescriptor.getReleaseVersion(),
                    "-Dtag=" + releaseDescriptor.getReleaseVersion(),
                    "-DreleaseVersion=" + releaseDescriptor.getReleaseVersion(),
                    "-DdevelopmentVersion=" + releaseDescriptor.getDevVersion(),
                    // "-DdryRun=true"
            };
            desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), prepareArgs
            );
            String[] performArgs = {
                    "-B", // non-interactive batch mode
                    "release:perform",
                    "-P", "gpg",
                    "-DskipTests",
                    "-Dgpg.pinentry-mode=default",
                    "-Darguments=\"-Dgpg.pinentry-mode=default\"",
                    // "-DdryRun=true"
            };
            desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), performArgs
            );

            releaseService.saveRelease(repo);
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }
}
