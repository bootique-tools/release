package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.service.preferences.PreferenceService;

import java.nio.file.Path;

public class MacOSService extends BaseDesktopService {

    public MacOSService(PreferenceService preferences) {
        super(preferences);
    }

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "open", "-a", "Terminal", path.toString());
    }

}
