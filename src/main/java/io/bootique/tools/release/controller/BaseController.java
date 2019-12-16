package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;

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

    List<Project> getSelectedProjects(String selectedProjects) throws IOException {
        List selectedProjectsId = objectMapper.readValue(selectedProjects, List.class);
        Organization organization = gitHubApi.getCurrentOrganization();
        return mavenService.getProjects(organization, project ->
                selectedProjectsId.contains(project.getRepository().getName()));
    }

    List<Project> getAllProjects() {
        Organization organization = gitHubApi.getCurrentOrganization();
        return mavenService.getProjects(organization, project -> true);
    }

    boolean haveMissingRepos(Organization organization) {
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            if (preferences.have(GitService.BASE_PATH_PREFERENCE)) {
                if(gitService.status(repository) == GitService.GitStatus.MISSING) {
                    return true;
                }
            }
        }
        return false;
    }
}
