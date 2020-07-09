package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.view.ReleaseContinueView;
import io.bootique.tools.release.view.ReleaseView;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/release")
public class ReleaseController extends BaseController {

    @Inject
    private LoggerService loggerService;

    @Inject
    private ReleaseService releaseService;

    @GET
    public Response home() {
        if (releaseService.hasCurrentActiveRelease()) {
            loggerService.prepareLogger(releaseService.getReleaseDescriptor());
            if (releaseService.getReleaseDescriptor().isAutoReleaseMode()) {
                releaseService.createThreadForRelease();
            }
            return Response.seeOther(URI.create("release/continue-release")).build();
        }

        return Response.seeOther(URI.create("release/start-release")).build();
    }

    @GET
    @Path("/continue-release")
    public ReleaseContinueView continueRelease(@Context UriInfo uriInfo) {
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        String lastSuccessStage = releaseDescriptor.getCurrentReleaseStage() != ReleaseStage.NO_RELEASE ?
                releaseDescriptor.getLastSuccessReleaseStage().getText() :
                releaseDescriptor.getCurrentRollbackStage().getText();

        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        User user = Ag.select(User.class, configuration).uri(uriInfo).get().getObjects().get(0);
        return new ReleaseContinueView(user, organization, releaseDescriptor.getReleaseVersion(), lastSuccessStage, releaseDescriptor.getProjectList());
    }

    @GET
    @Path("/start-release")
    public ReleaseView startRelease(@Context UriInfo uriInfo) {
        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        User user = Ag.select(User.class, configuration).uri(uriInfo).get().getObjects().get(0);
        return new ReleaseView(user, organization);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll(@Context UriInfo uriInfo) {
        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\",\"rootModule\",\"dependencies.dependencyProject.rootModule\"]")
                .build();

        return getProjects(project -> true, agRequest);
    }

    @GET
    @Path("show-projects")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showProjects(@Context UriInfo uriInfo, @QueryParam("version") String version) {

        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\",\"rootModule\"]")
                .build();
        DataResponse<Project> projectDataResponse = getProjects(project -> true, agRequest);

        projectDataResponse.getObjects().forEach(project -> project.setDisable(true));
        projectDataResponse.getObjects().forEach(project -> {
            if (project.getVersion().equals(version)) {
                project.setDisable(false);
                checkDependencies(project);
            }
        });

        return projectDataResponse;
    }

    @GET
    @Path("select-projects")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataResponse<Project> selectProjects(@QueryParam("version") String version, @QueryParam("projects") final String selected,
                                                @QueryParam("selectedProject") String selectedProject,
                                                @QueryParam("state") boolean state) throws IOException {
        List selectedProjects = objectMapper.readValue(selected, List.class);

        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\",\"rootModule\",\"rootModule.dependencies\"]")
                .build();
        DataResponse<Project> allProjects = getProjects(project -> true, agRequest);
        List<Project> selectedProjectsResp = allProjects.getObjects().stream()
                .filter(project -> selectedProjects.contains(project.getRepository().getName()))
                .collect(Collectors.toList());
        Optional<Project> haveProject = allProjects.getObjects().stream()
                .filter(p -> selectedProject.equals(p.getRepository().getName()))
                .findFirst();
        haveProject.ifPresent(project -> buildOrder(selectedProjectsResp, state, project, allProjects.getObjects()));

        filter(allProjects, selectedProjectsResp);
        return allProjects;
    }

    private void buildOrder(List<Project> selectedProjectsResp, boolean state, Project currentProject, List<Project> allProjects) {
        allProjects.forEach(project -> {
            if (state && !selectedProjectsResp.contains(project)) {
                currentProject.getDependencies().forEach(dependency -> {
                    if (dependency.getDependencyProject().getRootModule().equals(project.getRootModule())) {
                        selectedProjectsResp.add(project);
                        buildOrder(selectedProjectsResp, true, project, allProjects);
                    }
                });
            } else if (!state) {
                project.getDependencies().forEach(dependency -> {
                    if (dependency.getDependencyProject().getRootModule().equals(currentProject.getRootModule())) {
                        selectedProjectsResp.remove(project);
                        buildOrder(selectedProjectsResp, false, project, allProjects);
                    }
                });
            }
        });
    }

    private void filter(DataResponse<Project> allProjects, List<Project> selectedProjectsResp) {
        AgRequest agRequest = Ag.request(configuration).build();

        DataResponse<Project> dataResponse = getProjects(project -> true, agRequest);
        int flag = 0;
        for (Project selectedProjectResp : dataResponse.getObjects()) {
            for (Project project : selectedProjectsResp) {
                if (selectedProjectResp.compareTo(project) == 0) {
                    flag++;
                }
            }
            if (flag == 0) {
                allProjects.getObjects().remove(selectedProjectResp);
            }
            flag = 0;
        }
    }

    private void checkDependencies(Project project) {
        project.getDependencies().forEach(projectDependency -> {
            projectDependency.getDependencyProject().setDisable(false);
            checkDependencies(projectDependency.getDependencyProject());
        });
    }
}
