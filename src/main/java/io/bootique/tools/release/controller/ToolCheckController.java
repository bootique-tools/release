package io.bootique.tools.release.controller;

import java.nio.file.Paths;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.bootique.tools.release.model.persistent.*;
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

        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));

        return new ToolCheckView(gitHubApi.getCurrentUser(), organization,
                javaVersion, jdk, mavenVersion);
    }

}
