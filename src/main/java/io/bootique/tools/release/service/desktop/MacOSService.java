package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class MacOSService extends BaseDesktopService {

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "open", "-a", "Terminal", path.toString());
    }

}
