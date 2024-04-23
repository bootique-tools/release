package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class LinuxDesktopService extends BaseDesktopService {

    public LinuxDesktopService(String javaHome) {
        super(javaHome);
    }

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "xterm");
    }
}
