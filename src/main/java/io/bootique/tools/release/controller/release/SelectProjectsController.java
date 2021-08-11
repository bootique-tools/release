package io.bootique.tools.release.controller.release;

import io.agrest.DataResponse;
import io.bootique.tools.release.controller.BaseController;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.view.ReleaseView;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/release")
public class SelectProjectsController extends BaseController {

    @Inject
    public ReleasePersistentService persistentService;

    @Path("/start")
    @GET
    public Response home() {
        if (persistentService.isReleaseSaved()) {
            return Response.seeOther(URI.create("release/")).build();
        }
        return Response.seeOther(URI.create("release/select-projects")).build();
    }

    @GET
    @Path("/select-projects")
    public ReleaseView getReleaseView() {
        return new ReleaseView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll() {
        return fetchProjects("[\"repository\",\"rootModule\",\"dependencies.repository\"]");
    }

    @GET
    @Path("show-projects")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showProjects(@QueryParam("version") String version) {
        DataResponse<Project> projectDataResponse = fetchProjects("[\"dependencies\",\"repository\",\"rootModule\"]");

        projectDataResponse.getObjects().forEach(project -> {
            project.setDisable(true);
            if (project.getVersion().equals(version)) {
                project.setDisable(false);
                checkDependencies(project);
            }
        });

        return projectDataResponse;
    }

    @GET
    @Path("project")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataResponse<Project> getProjects(@QueryParam("version") String version,
                                                @QueryParam("projects") final String selected,
                                                @QueryParam("selectedProject") String selectedProject,
                                                @QueryParam("state") boolean state) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> selectedProjects = objectMapper.readValue(selected, List.class);
        DataResponse<Project> allProjects = fetchProjects("[\"repository\",\"rootModule\",\"rootModule.dependencies\"]");
        List<Project> selectedProjectsResp = allProjects.getObjects().stream()
                .filter(project -> selectedProjects.contains(project.getRepository().getName()) && project.getVersion().equals(version))
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
                    if (dependency.getRootModule().equals(project.getRootModule())
                            && dependency.getVersion().equals(currentProject.getVersion())) {
                        selectedProjectsResp.add(project);
                        buildOrder(selectedProjectsResp, true, project, allProjects);
                    }
                });
            } else if (!state && selectedProjectsResp.contains(project)) {
                project.getDependencies().forEach(dependency -> {
                    if (dependency.getRootModule().equals(currentProject.getRootModule())
                            && dependency.getVersion().equals(project.getVersion())) {
                        selectedProjectsResp.remove(project);
                        buildOrder(selectedProjectsResp, false, project, allProjects);
                    }
                });
            }
        });
    }

    private void filter(DataResponse<Project> allProjects, List<Project> selectedProjectsResp) {
        DataResponse<Project> dataResponse = fetchProjects();

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
            if (projectDependency.isDisable() && projectDependency.getVersion().equals(project.getVersion())) {
                checkDependencies(projectDependency);
                projectDependency.setDisable(false);
            }
        });
    }
}
