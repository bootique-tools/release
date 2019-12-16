package io.bootique.tools.release.controller;

import java.nio.file.Paths;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.view.ToolCheckView;

@Path("/tool-check")
public class ToolCheckController extends BaseController {

    @Inject
    private DesktopService desktopService;

    @GET
    public ToolCheckView home() {

        String javaVersion;
        try {
            javaVersion = desktopService.runCommand(Paths.get("."), "java", "-version");
        } catch (Exception ex) {
            javaVersion = "Unable to run 'java', " + ex.getMessage();
        }
        String mavenVersion;
        try {
            mavenVersion = desktopService.runCommand(Paths.get("."), "mvn", "-version");
        } catch (Exception ex) {
            mavenVersion = "Unable to run 'mvn', " + ex.getMessage();
        }

        String jdk;
        try {
            jdk = desktopService.runCommand(Paths.get("."), "jdk");
        } catch (Exception ex) {
            jdk = "Unable to run 'jdk' command, " + ex.getMessage();
        }

        // /usr/libexec/java_home -v $1
        try {
            String javaHome = desktopService.runCommand(Paths.get("."), "/usr/libexec/java_home", "-v", "1.8");
            javaVersion += "\n" + javaHome;
//            javaVersion = desktopService.runCommand(Paths.get("."), "java", "-version");
        } catch (Exception ex) {
            // TODO: ???
        }

        return new ToolCheckView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization(),
                javaVersion, jdk, mavenVersion);
    }

}
