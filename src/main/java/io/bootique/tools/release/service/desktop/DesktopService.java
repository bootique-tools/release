package io.bootique.tools.release.service.desktop;

import java.io.File;
import java.nio.file.Path;

public interface DesktopService {

    File selectFile();

    void openFolder(Path path);

    void openTerminal(Path path);

    String runCommand(Path path, String command, String... args);

    String runMavenCommand(Path path, String... args);

}
