package io.bootique.tools.release.service.release.persistent;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.preferences.Preference;

import java.io.IOException;

public interface ReleasePersistentService {

    Preference<String> SAVE_PATH = Preference.of("save.path", String.class);

    void saveRelease();

    boolean isReleaseSaved();

    ReleaseDescriptor loadRelease() throws IOException;

    void deleteRelease();

}
