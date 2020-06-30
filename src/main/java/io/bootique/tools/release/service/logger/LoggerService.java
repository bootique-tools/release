package io.bootique.tools.release.service.logger;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.dto.ReleaseDescriptorDTO;
import io.bootique.tools.release.service.preferences.Preference;

public interface LoggerService {

    Preference<String> LOGGER_BASE_PATH = Preference.of("logger.base.path", String.class);

    void prepareLogger(ReleaseDescriptor releaseDescriptor);

    void setAppender(String repository, String action, String file);
}
