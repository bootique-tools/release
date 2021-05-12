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
import java.util.Collections;
import java.util.Comparator;
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

    protected List<Project> getSelectedProjects(String selectedProjects) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> selectedProjectsNames = objectMapper.readValue(selectedProjects, List.class);
        List<Project> projectList = ObjectSelect.query(Project.class)
                .select(cayenneRuntime.newContext())
                .stream()
                .filter(project -> selectedProjectsNames.contains(project.getRepository().getName()))
                .collect(Collectors.toList());
        return sortProjects(selectedProjectsNames, projectList);
    }

    private boolean haveMissingRepos() {
        for (Repository repository : getCurrentOrganization().getRepositories()) {
            if (preferences.have(GitService.BASE_PATH_PREFERENCE)) {
                if (gitService.status(repository) == GitService.GitStatus.MISSING
                        && !excludedProject(repository)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean excludedProject(Repository repository) {
        return repository.getName().equals("bootique-rabbitmq-client")
                || repository.getName().equals("bootique-jersey-client");
    }

    protected DataResponse<Project> getProjects(Predicate<Project> predicate, AgRequest request) {

        DataResponse<Project> projectDataResponse = Ag.select(Project.class, configuration).request(request).get();
        List<Project> projects = projectDataResponse.getObjects();
        if (projects.isEmpty()) {
            if(!createProjects(predicate).isEmpty()) {
                return getProjects(predicate, request);
            } else {
                return DataResponse.forObjects(Collections.emptyList());
            }
        }

        List<Project> filteredProjects = projects.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        projectDataResponse.setObjects(filteredProjects);
        return projectDataResponse;
    }

    private List<Project> sortProjects(List<String> selectedProjectsName, List<Project> selectedProjects) {
        selectedProjects.sort(Comparator.comparingInt(p -> selectedProjectsName.indexOf(p.getRepository().getName())));
        return selectedProjects;
    }

    private List<Project> createProjects(Predicate<Project> predicate) {
        Organization organization = getCurrentOrganization();

        List<Project> projects = haveMissingRepos()
                ? Collections.emptyList()
                : mavenService.getProjects(organization, predicate);

        if (!projects.isEmpty()) {
            projects.forEach(project -> {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                project.setDisable(true);
            });

            organization.getObjectContext().commitChanges();
        }

        return projects;
    }
}
