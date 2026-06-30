package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.service.preferences.PreferenceService;

import java.nio.file.Path;

public class GenericDesktopService extends BaseDesktopService {
    public GenericDesktopService(PreferenceService preferences) {
        super(preferences);
    }

    @Override
    public void openTerminal(Path path) {
        throw new UnsupportedOperationException("Unsupported platform");
    }
}
