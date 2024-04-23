package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class MacOSService extends BaseDesktopService {

    public MacOSService(String javaHome) {
        super(javaHome);
    }

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "open", "-a", "Terminal", path.toString());
    }

}
