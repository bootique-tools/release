package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.view.ReleaseContinueView;
import io.bootique.tools.release.view.ReleaseView;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/release")
public class ReleaseController extends BaseController {

    @Inject
    private MavenService mavenService;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseService releaseService;

    @GET
    public Response home() {
        if(releaseService.hasCurrentActiveRelease()) {
            loggerService.prepareLogger(releaseService.getReleaseDescriptor());
            if(releaseService.getReleaseDescriptor().isAutoReleaseMode()) {
                releaseService.createThreadForRelease();
            }
            return Response.seeOther(URI.create("release/continue-release")).build();
        }

        return Response.seeOther(URI.create("release/start-release")).build();
    }

    @GET
    @Path("/continue-release")
    public ReleaseContinueView continueRelease(){
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        String currentStage;
        currentStage = releaseDescriptor.getCurrentReleaseStage() != ReleaseStage.NO_RELEASE ?
                releaseDescriptor.getCurrentReleaseStage().getText() :
                releaseDescriptor.getCurrentRollbackStage().getText();
        return new ReleaseContinueView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization(), releaseDescriptor.getReleaseVersion(), currentStage, releaseDescriptor.getProjectList());
    }

    @GET
    @Path("/start-release")
    public ReleaseView startRelease(){
        return new ReleaseView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization());
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> showAll() {
        Organization organization = gitHubApi.getCurrentOrganization();
        return haveMissingRepos(organization) ? new ArrayList<>() :
                mavenService.getProjects(organization, project -> {
                    project.setDisable(true);
                    return true;
                });
    }

    @GET
    @Path("show-projects")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> showProjects(@QueryParam("version") String version) {
        Organization organization = gitHubApi.getCurrentOrganization();

        return haveMissingRepos(organization) ? new ArrayList<>() :
                mavenService.getProjects(organization, project -> {
                    if (project.getVersion().equals(version)) {
                        project.setDisable(false);
                    } else {
                        project.setDisable(true);
                    }
                    return true;});
    }

    @GET
    @Path("select-projects")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> selectProjects(@QueryParam("version") String version, @QueryParam("projects") final String selected,
                                           @QueryParam("selectedProject") String selectedProject,
                                           @QueryParam("state") boolean state) throws IOException {
        List selectedProjects = objectMapper.readValue(selected, List.class);
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Project> allProjects = mavenService.getProjects(organization, project -> true);
        List<Project> selectedProjectsResp = allProjects.stream()
                .filter(project -> selectedProjects.contains(project.getRepository().getName()))
                .collect(Collectors.toList());
        Optional<Project> haveProject = allProjects.stream()
                .filter(p -> selectedProject.equals(p.getRepository().getName()))
                .findFirst();
        haveProject.ifPresent(project -> buildOrder(selectedProjectsResp, state, project, allProjects));
        return selectedProjectsResp;
    }

    private void buildOrder(List<Project> selectedProjectsResp, boolean state, Project currentProject, List<Project> allProjects) {
        allProjects.forEach(project -> {
            if(state && currentProject.getDependencies().contains(project) && !selectedProjectsResp.contains(project)) {
                selectedProjectsResp.add(project);
                buildOrder(selectedProjectsResp, true, project, allProjects);
            } else if(!state && project.getDependencies().contains(currentProject)) {
                selectedProjectsResp.remove(project);
                buildOrder(selectedProjectsResp, false, project, allProjects);
            }
        });
    }
}
