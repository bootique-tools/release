package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import java.io.IOException;
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
    GitHubApi gitHubApi;

    @Inject
    MavenService mavenService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    GitService gitService;

    @Inject
    ContentService contentService;

    @Context
    Configuration configuration;

    List<Project> getSelectedProjects(String selectedProjects) throws IOException {
        List selectedProjectsId = objectMapper.readValue(selectedProjects, List.class);
        AgRequest agRequest = Ag.request(configuration).build();

        DataResponse<Project> projects = Ag.select(Project.class, configuration).request(agRequest).get();

        return projects.getObjects().stream().filter(project ->
                selectedProjectsId.contains(project.getRepository().getName())).collect(Collectors.toList());
    }

    List<Project> getAllProjects() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        return mavenService.getProjects(organization, project -> true);
    }

    boolean haveMissingRepos(Organization organization) {
        for (Repository repository : organization.getRepositories()) {
            if (preferences.have(GitService.BASE_PATH_PREFERENCE)) {
                if (gitService.status(repository) == GitService.GitStatus.MISSING) {
                    return true;
                }
            }
        }
        return false;
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
            projectDataResponse.setObjects(projects);
        }

        return projectDataResponse;
    }
}
