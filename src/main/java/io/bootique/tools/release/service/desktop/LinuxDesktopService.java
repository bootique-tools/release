package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class LinuxDesktopService extends BaseDesktopService {

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "xterm");
    }
}
