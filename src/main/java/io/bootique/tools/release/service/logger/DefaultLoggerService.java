package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import javax.inject.Inject;

public class DefaultLoggerService implements LoggerService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DesktopService.class);

    @Inject
    PreferenceService preferenceService;

    private MultiAppender multiAppender;
    private String releaseVersion;

    public void prepareLogger(ReleaseDescriptor releaseDescriptor) {
        if (multiAppender == null) {
            multiAppender = (MultiAppender) LOGGER.getAppender("multiAppender");
            multiAppender.createAppenderMap(releaseDescriptor, preferenceService.get(LoggerService.LOGGER_BASE_PATH));
            this.releaseVersion = releaseDescriptor.getReleaseVersions().releaseVersion();
        }
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
