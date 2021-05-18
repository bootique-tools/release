package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.RootResourceEntity;
import io.agrest.SelectStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.persistent.User;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
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

    protected DataResponse<Project> fetchProjects() {
        return fetchProjects(null);
    }

    protected DataResponse<Project> fetchProjects(String include) {
        AgRequest request = Ag.request(configuration).addInclude(include).build();
        return Ag.select(Project.class, configuration)
                .stage(SelectStage.FETCH_DATA, new MavenProjectSorter(mavenService))
                .request(request)
                .get();
    }

    protected List<Project> getSelectedProjects(String selectedProjects) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> selectedProjectsNames = objectMapper.readValue(selectedProjects, List.class);
        List<Project> projectList = ObjectSelect.query(Project.class)
                .where(Project.REPOSITORY.dot(Repository.NAME).in(selectedProjectsNames))
                .select(cayenneRuntime.newContext());
        return sortProjectsBySelection(selectedProjectsNames, projectList);
    }

    private List<Project> sortProjectsBySelection(List<String> selectedProjectsName, List<Project> selectedProjects) {
        selectedProjects.sort(Comparator.comparingInt(p -> selectedProjectsName.indexOf(p.getRepository().getName())));
        return selectedProjects;
    }

    static class MavenProjectSorter implements Consumer<SelectContext<Project>> {

        private final MavenService mavenService;

        MavenProjectSorter(MavenService mavenService) {
            this.mavenService = mavenService;
        }

        @Override
        public void accept(SelectContext<Project> context) {
            RootResourceEntity<Project> entity = context.getEntity();
            entity.setResult(mavenService.sortProjects(entity.getResult()));
        }
    }
}
