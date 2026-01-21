package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;

public class ReleaseExecutionLogger implements ExecutionLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseExecutionLogger.class);

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseDescriptorService releaseDescriptorService;

    @Override
    public String getLogs(RepositoryDescriptor repositoryDescriptor, ReleaseStage releaseStage) {
        var fileAppender = getFileAppender(repositoryDescriptor, releaseStage);
        try {
            return String.join("\n", Files.readAllLines(Path.of(fileAppender.getFile())));
        } catch (NoSuchFileException e) {
            return "Logfile not found: " + fileAppender.getFile();
        } catch (IOException | NullPointerException e) {
            LOGGER.warn("Unable to get logs for the stage {}", releaseStage, e);
        }
        return "";
    }

    @Override
    public void writeLogs(String repositoryName, ReleaseStage stage, String text) {
        try {
            loggerService.setAppender(repositoryName, "release", String.valueOf(stage));
            LOGGER.info(text);
        } catch (NullPointerException e) {
            LOGGER.warn("Unable to write logs for the stage {}", stage, e);
        }
    }

    private FileAppender<ILoggingEvent> getFileAppender(RepositoryDescriptor repositoryDescriptor, ReleaseStage releaseStage) {
        return loggerService
                .getMultiAppender()
                .getAppenderMap()
                .get(Arrays.asList(releaseDescriptorService.getReleaseDescriptor().getReleaseVersions().releaseVersion(),
                        repositoryDescriptor.getRepositoryName(), "release", releaseStage.toString())
                );
    }
}
