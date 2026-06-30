package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.service.preferences.PreferenceService;

import java.nio.file.Path;

public class LinuxDesktopService extends BaseDesktopService {

    public LinuxDesktopService(PreferenceService preferences) {
        super(preferences);
    }

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "xterm");
    }
}
