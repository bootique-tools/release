package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.util.CopyDirVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseDesktopServiceTest {

    private DesktopService desktopService;

    @BeforeEach
    void createService() {
        desktopService = createDesktopService();
    }

    private DesktopService createDesktopService() {
        String os = System.getProperty("os.name").toLowerCase();
        String javaHome = "/usr/libexec/java_home -v 11";
        if(os.contains("win")) {
            return new WindowsDesktopService(javaHome);
        }
        if(os.contains("mac")) {
            return new MacOSService(javaHome);
        }
        if(os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxDesktopService(javaHome);
        }
        return new GenericDesktopService(javaHome);
    }

    @Test
    @DisplayName("Running desktop command test")
    void testRunningDesktopCommand() {
        String result = desktopService.runCommand(Paths.get(System.getProperty("user.home")), "echo", "test");
        assertEquals(result.trim(), "test");
    }

    // todo run maven command not work
    @Test
    @DisplayName("Running maven command test")
    void testRunningMavenCommand(@TempDir Path tempPath) throws IOException {
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "dummy-org-00" + File.separator + "dummy-api");
        Files.walkFileTree(path, new CopyDirVisitor(path, tempPath, StandardCopyOption.REPLACE_EXISTING));

        String output = desktopService.runMavenCommand(tempPath, "clean", "install");
        for(String expectedResult : EXPECTED_RESULTS) {
            assertTrue(output.contains(expectedResult), "Expected string \"" + expectedResult + "\" not found.");
        }
    }

    private final String[] EXPECTED_RESULTS = {
            "[INFO] Scanning for projects...",
            "[INFO] Building dummy-api 1.0.6-SNAPSHOT",
            "[INFO] No sources to compile",
            "[INFO] No tests to run.",
            "[WARNING] JAR will be empty - no content was marked for inclusion!",
            "[INFO] BUILD SUCCESS"
    };
}
