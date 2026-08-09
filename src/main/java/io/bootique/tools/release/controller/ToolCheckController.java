package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.tools.ToolCheckResponse;
import io.bootique.tools.release.service.desktop.DesktopService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.nio.file.Paths;

@Path("/tool-check")
public class ToolCheckController extends BaseController {

    @Inject
    private DesktopService desktopService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ToolCheckResponse check() {

        String javaVersion;
        java.nio.file.Path currentDir = Paths.get(".");
        try {
            javaVersion = desktopService.runCommand(currentDir, "java", "-version");
        } catch (Exception ex) {
            javaVersion = "Unable to run 'java', " + ex.getMessage();
        }
        String mavenVersion;
        try {
            mavenVersion = desktopService.runCommand(currentDir, "mvn", "-version");
        } catch (Exception ex) {
            mavenVersion = "Unable to run 'mvn', " + ex.getMessage();
        }

        String jdk;
        try {
            jdk = desktopService.runCommand(currentDir, "jdk");
        } catch (Exception ex) {
            jdk = "Unable to run 'jdk' command, " + ex.getMessage();
        }

        // /usr/libexec/java_home -v $1
        try {
            String javaHome = desktopService.runCommand(currentDir, "/usr/libexec/java_home", "-v", "1.8");
            javaVersion += "\n" + javaHome;
//            javaVersion = desktopService.runCommand(Paths.get("."), "java", "-version");
        } catch (Exception ex) {
            // TODO: ???
        }

        return new ToolCheckResponse(javaVersion, jdk, mavenVersion);
    }

}
