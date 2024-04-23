package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.service.preferences.Preference;

import java.io.File;
import java.nio.file.Path;

public interface DesktopService {

    Preference<String> JAVA_HOME = Preference.of("javaHome", String.class);

    File selectFile();

    void openFolder(Path path);

    void openTerminal(Path path);

    String runCommand(Path path, String command, String... args);

    String runMavenCommand(Path path, String... args);

    /**
     * Perform mvn release:operation, made a separate method to better control and modify parameters.
     *
     * @param path to the directory where to perform release operation
     * @param operation type (prepare, perform, rollback or clean)
     * @return output
     */
    String performReleasePlugin(Path path, String operation, String additionalArgs);

}
