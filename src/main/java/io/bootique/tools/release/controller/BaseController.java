package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.User;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

abstract class BaseController {

    @Inject
    PreferenceService preferences;

    @Inject
    MavenService mavenService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    GitService gitService;

    @Context
    Configuration configuration;

    @Inject
    ServerRuntime cayenneRuntime;

    private transient Organization currentOrganization;
    private transient User currentUser;

    protected Organization getCurrentOrganization() {
        if(currentOrganization == null) {
            currentOrganization = ObjectSelect.query(Organization.class).selectFirst(cayenneRuntime.newContext());
        }
        return currentOrganization;
    }

    protected User getCurrentUser() {
        if(currentUser == null) {
            currentUser = ObjectSelect.query(User.class).selectFirst(cayenneRuntime.newContext());
        }
        return currentUser;
    }



    List<Project> getSelectedProjects(String selectedProjects) throws IOException {
        List selectedProjectsName = objectMapper.readValue(selectedProjects, List.class);
        AgRequest agRequest = Ag.request(configuration).build();

        DataResponse<Project> projects = Ag.select(Project.class, configuration).request(agRequest).get();

        List<Project> projectList = projects.getObjects().stream().filter(project ->
                selectedProjectsName.contains(project.getRepository().getName())).collect(Collectors.toList());
        return sortProjects(selectedProjectsName, projectList);
    }

    List<Project> getAllProjects() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        return mavenService.getProjects(organization, project -> true);
    }

    boolean haveMissingRepos(Organization organization) {
        for (Repository repository : organization.getRepositories()) {
            if (preferences.have(GitService.BASE_PATH_PREFERENCE)) {
                if (gitService.status(repository) == GitService.GitStatus.MISSING
                        && !excludedProject(repository)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean excludedProject(Repository repository) {
        return repository.getName().equals("bootique-rabbitmq-client")
                || repository.getName().equals("bootique-jersey-client");
    }

    private DataResponse<Project> createProject(Predicate<Project> predicate) {
        AgRequest agRequestOrganization = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequestOrganization).get().getObjects().get(0);

        List<Project> projects = haveMissingRepos(organization) ? Collections.emptyList() :
                mavenService.getProjects(organization, predicate);

        if (!projects.isEmpty()) {
            projects.forEach(project -> {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                project.setDisable(true);
            });

            organization.getObjectContext().commitChanges();

        }

        return DataResponse.forObjects(projects);
    }

    protected DataResponse<Project> getProjects(Predicate<Project> predicate, AgRequest agRequest) {

        DataResponse<Project> projectDataResponse = Ag.select(Project.class, configuration).request(agRequest).get();

        if (projectDataResponse.getObjects().size() == 0) {

            projectDataResponse = createProject(predicate);

            if (!projectDataResponse.getObjects().isEmpty()) {
                return getProjects(predicate, agRequest);
            }

        } else {
            List<Project> projects = projectDataResponse.getObjects().stream().filter(predicate)
                    .collect(Collectors.toList());

            projects = mavenService.sortMavenProject(projects);

            projectDataResponse.setObjects(projects);
        }

        return projectDataResponse;
    }

    private List<Project> sortProjects(List<String> selectedProjectsName, List<Project> selectedProjects) {
        List<Project> sortedProjects = new ArrayList<>();

        for(String projectName : selectedProjectsName) {
            sortedProjects.add(
                    selectedProjects.stream().filter(
                            project -> projectName.equals(project.getRepository().getName()))
                            .findFirst().orElseThrow());
        }

        return sortedProjects;
    }
}
