package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import javax.inject.Inject;

public class DefaultLoggerService implements LoggerService {

    @Inject
    PreferenceService preferenceService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private MultiAppender multiAppender;
    private ReleaseDescriptor releaseDescriptor;

    @Override
    public void prepareLogger(ReleaseDescriptor releaseDescriptor) {
        multiAppender = (MultiAppender) logger.getAppender("multiAppender");
        multiAppender.createAppenderMap(releaseDescriptor,  preferenceService.get(LoggerService.LOGGER_BASE_PATH));
        this.releaseDescriptor = releaseDescriptor;
    }

    @Override
    public void setAppender(String repository, String action, String file) {
        multiAppender.setCurrentAppender(Arrays.asList(releaseDescriptor.getReleaseVersion(), repository, action, file));
    }

    MultiAppender getMultiAppender() {
        return multiAppender;
    }
}
