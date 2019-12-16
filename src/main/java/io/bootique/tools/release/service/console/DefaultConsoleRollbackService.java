package io.bootique.tools.release.service.console;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

public class DefaultConsoleRollbackService implements ConsoleRollbackService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultConsoleReleaseService.class);

    @Inject
    private Map<RollbackStage, Function<Repository, String>> rollbackMap;

    @Inject
    ReleaseService releaseService;

    @Inject
    private LoggerService loggerService;

    @Override
    public boolean checkReadyForRollback() {
        if(!releaseService.hasCurrentActiveRelease()) {
            LOGGER.info("No release to rollback.");
            return false;
        }
        if(releaseService.getReleaseDescriptor().getCurrentReleaseStage() == ReleaseStage.RELEASE_SYNC) {
            LOGGER.info("Can't rollback because current stage is sync with maven central. ");
            return false;
        }
        return true;
    }

    @Override
    public void startRollbackFromConsole() {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        loggerService.prepareLogger(releaseService.getReleaseDescriptor());
        releaseDescriptor.setCurrentReleaseStage(ReleaseStage.NO_RELEASE);

        for(RollbackStage rollbackStage : RollbackStage.values()) {
            if(rollbackStage == RollbackStage.NO_ROLLBACK) {
                continue;
            }
            releaseDescriptor.setCurrentRollbackStage(rollbackStage);
            for(Project project : releaseDescriptor.getProjectList()) {
                try {
                    rollbackMap.get(rollbackStage).apply(project.getRepository());
                    LOGGER.info("Current stage: " + rollbackStage + ". " + project.getRepository().getName() + " - done.");
                } catch (JobException ex) {
                    throw new JobException(ex.getMessage(), ex);
                }
            }
            if(rollbackStage == RollbackStage.ROLLBACK_MVN) {
                releaseService.deleteLock();
            }
        }
    }

}
