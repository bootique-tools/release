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

class BaseDesktopServiceTest {

    private DesktopService desktopService;

    @BeforeEach
    void createService() {
        desktopService = createDesktopService();
    }

    private DesktopService createDesktopService() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            return new WindowsDesktopService();
        }
        if(os.contains("mac")) {
            return new MacOSService();
        }
        if(os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxDesktopService();
        }
        return new GenericDesktopService();
    }

    @Test
    @DisplayName("Running desktop command test")
    void testRunningDesktopCommand() {
        String result = desktopService.runCommand(Paths.get(System.getProperty("user.home")), "echo", "test");
        assertEquals(result.trim(), "test");
    }

    @Test
    @DisplayName("Running maven command test")
    void testRunningMavenCommand(@TempDir Path tempPath) throws IOException {
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "dummy-org-00" + File.separator + "dummy-api");
        Files.walkFileTree(path, new CopyDirVisitor(path, tempPath, StandardCopyOption.REPLACE_EXISTING));
        String result = "[INFO] Scanning for projects...\n" +
                "[INFO] \n" +
                "[INFO] ----------------------< dummy-org-api:dummy-api >-----------------------\n" +
                "[INFO] Building dummy-api 1.0.6-SNAPSHOT\n" +
                "[INFO] --------------------------------[ jar ]---------------------------------\n" +
                "[INFO] \n" +
                "[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ dummy-api ---\n" +
                "[INFO] \n" +
                "[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ dummy-api ---\n" +
                "[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!\n" +
                "[INFO] skip non existing resourceDirectory " + "/private" + tempPath.toString() + "/src/main/resources\n" +
                "[INFO] \n" +
                "[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ dummy-api ---\n" +
                "[INFO] No sources to compile\n" +
                "[INFO] \n" +
                "[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ dummy-api ---\n" +
                "[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!\n" +
                "[INFO] skip non existing resourceDirectory " + "/private" + tempPath.toString() + "/src/test/resources\n" +
                "[INFO] \n" +
                "[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ dummy-api ---\n" +
                "[INFO] No sources to compile\n" +
                "[INFO] \n" +
                "[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ dummy-api ---\n" +
                "[INFO] No tests to run.\n" +
                "[INFO] \n" +
                "[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ dummy-api ---\n" +
                "[WARNING] JAR will be empty - no content was marked for inclusion!\n" +
                "[INFO] Building jar: " + "/private" + tempPath.toString() + "/target/dummy-api-1.0.6-SNAPSHOT.jar\n" +
                "[INFO] \n" +
                "[INFO] --- maven-install-plugin:2.4:install (default-install) @ dummy-api ---\n";
        String output = desktopService.runMavenCommand(tempPath, "clean", "install");
        String[] resArr = result.split("\n");
        String[] outputArr = output.split("\n");
        for(int i = 0; i < resArr.length - 9; i++) {
            assertEquals(resArr[i], outputArr[i]);
        }
    }
}
