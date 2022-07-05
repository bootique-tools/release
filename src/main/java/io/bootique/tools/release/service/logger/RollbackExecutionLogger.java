package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static io.bootique.tools.release.model.release.ReleaseStage.RELEASE_PERFORM;

public class RollbackExecutionLogger implements ExecutionLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopService.class);

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseDescriptorService releaseDescriptorService;

    @Override
    public String getLogs(RepositoryDescriptor repositoryDescriptor, ReleaseStage releaseStage) {
        try {
            RollbackStage rollbackStage = switch (releaseStage) {
                case RELEASE_PREPARE -> RollbackStage.ROLLBACK_MVN;
                case RELEASE_PERFORM -> RollbackStage.ROLLBACK_SONATYPE;
                default              -> RollbackStage.NO_ROLLBACK;
            };
            var fileAppender = getFileAppender(repositoryDescriptor, rollbackStage);
            return String.join("\n", Files.readAllLines(Path.of(fileAppender.getFile())));
        } catch (IOException | NullPointerException e) {
            LOGGER.warn("Unable to get logs for the stage " + releaseStage, e);
        }
        return "";
    }

    @Override
    public void writeLogs(String repositoryName, String stage, String text) {
        try {
            loggerService.setAppender(repositoryName, "rollback", stage);
            LOGGER.info(text);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private FileAppender<ILoggingEvent> getFileAppender(RepositoryDescriptor repositoryDescriptor, RollbackStage rollbackStage) throws IOException {
        return loggerService
                .getMultiAppender()
                .getAppenderMap()
                .get(Arrays.asList(releaseDescriptorService.getReleaseDescriptor().getReleaseVersions().releaseVersion(),
                        repositoryDescriptor.getRepositoryName(), "rollback", rollbackStage.toString())
                );
    }
}
