package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import jakarta.inject.Inject;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DefaultLoggerService implements LoggerService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DesktopService.class);

    @Inject
    PreferenceService preferenceService;

    private MultiAppender multiAppender;
    private String releaseVersion;
    private ReleaseDescriptor preparedDescriptor;

    public void prepareLogger(ReleaseDescriptor releaseDescriptor) {
        // rebuilding the appender map clears it, which would drop output of a stage running right now,
        // and callers may ask repeatedly for the same release. Keyed on the descriptor instance rather
        // than its version, so dropping a release and starting another at the same version re-prepares.
        if (multiAppender != null && releaseDescriptor == preparedDescriptor) {
            return;
        }

        if (multiAppender != null) {
            multiAppender.getAppenderMap().clear();
        } else {
            multiAppender = (MultiAppender) LOGGER.getAppender("multiAppender");
        }
        multiAppender.createAppenderMap(releaseDescriptor, preferenceService.get(LoggerService.LOGGER_BASE_PATH));
        this.releaseVersion = releaseDescriptor.getReleaseVersions().releaseVersion();
        this.preparedDescriptor = releaseDescriptor;
    }

    @Override
    public void setAppender(String repository, String action, String file) {
        multiAppender.setCurrentAppender(Arrays.asList(releaseVersion, repository, action, file));
    }

    @Override
    public MultiAppender getMultiAppender() {
        return multiAppender;
    }
}
