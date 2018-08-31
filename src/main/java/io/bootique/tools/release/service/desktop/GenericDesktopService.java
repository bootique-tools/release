package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class GenericDesktopService extends BaseDesktopService {
    @Override
    public void openTerminal(Path path) {
        throw new UnsupportedOperationException("Unsupported platform");
    }
}
