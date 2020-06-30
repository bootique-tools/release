package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.github.GitHubRestAPI;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.view.MilestonesView;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("milestone")
public class MilestoneController extends DefaultBaseController {

    private final String CONTROLLER_NAME = "milestone";

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private MavenService mavenService;

    @Inject
    private GitHubRestAPI gitHubRestAPI;

    @GET
    public MilestonesView home(@Context UriInfo uriInfo) {
        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));
        return new MilestonesView(gitHubApi.getCurrentUser(), organization);
    }

    @GET
    @Path("/getMilestones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMilestones(@QueryParam("selectedModules") String selectedModules) throws IOException {
        List selectedProjects = objectMapper.readValue(selectedModules, List.class);
        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository.milestones\"," +
                        "{\"path\":\"repository.milestones\",\"cayenneExp\":\"state like \'OPEN\'\"}]")
                .build();
        DataResponse<Project> projects = getProjects(project -> selectedProjects.contains(project.getRepository().getName()), agRequest);
        Map<String, Integer> milestoneMap = new HashMap<>();
        for (Project project : projects.getObjects()) {
            for (Milestone milestone : project.getRepository().getMilestones()) {
                if (milestone.getState().equals("OPEN")) {
                    if (!milestoneMap.containsKey(milestone.getTitle())) {
                        milestoneMap.put(milestone.getTitle(), 1);
                    } else {
                        milestoneMap.put(milestone.getTitle(), milestoneMap.get(milestone.getTitle()) + 1);
                    }
                }
            }
        }
        List<String> milestones = new ArrayList<>();
        for (String key : milestoneMap.keySet()) {
            if (milestoneMap.get(key) == projects.getObjects().size()) {
                milestones.add(key);
            }
        }
        return milestones;
    }

    @GET
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(@QueryParam("milestoneNewTitle") String title,
                       @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Milestone milestone = gitHubRestAPI.createMilestone(project.getRepository(), title, "");
                project.getRepository().addMilestoneToCollection(milestone);
                milestone.getObjectContext().commitChanges();
                return "";
            } catch (IOException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

    @GET
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    public void close(@QueryParam("milestoneTitle") String title,
                      @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                gitHubRestAPI.closeMilestone(project.getRepository(), title, "");
                repository.closeMilestone(title);
                repository.getObjectContext().commitChanges();
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

    @GET
    @Path("/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    public void rename(@QueryParam("milestoneTitle") String title,
                       @QueryParam("selectedModules") String selectedModules, @QueryParam("milestoneNewTitle") String milestoneNewTitle) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                gitHubRestAPI.renameMilestone(repository, title, "", milestoneNewTitle);
                repository.renameMilestone(title, milestoneNewTitle);
                repository.getObjectContext().commitChanges();
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };

        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll(@Context UriInfo uriInfo) {
        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\",\"modules\",\"rootModule\",\"repository.milestones\",\"repository.milestones.issues\"," +
                        "{\"path\":\"repository.milestones\",\"cayenneExp\":\"state like \'OPEN\'\"}]")
                .build();
        return getProjects(project -> true, agRequest);
    }

    private DataResponse<Project> getProjects(Predicate<Project> predicate, AgRequest agRequest) {

        DataResponse<Project> projectDataResponse = Ag.select(Project.class, configuration).request(agRequest).get();

        if (projectDataResponse.getObjects().size() == 0) {

            AgRequest agRequestOrganization = Ag.request(configuration).build();
            Organization organization = Ag.select(Organization.class, configuration).request(agRequestOrganization).get().getObjects().get(0);
            contentService.getMilestones(organization)
                    .forEach(milestone ->
                            milestone.setIssues(
                                    contentService.getIssues(organization,
                                            List.of(issue -> (milestone.equals(issue.getMilestone()) && milestone.getRepository().equals(issue.getRepository()))),
                                            Comparator.comparing(Issue::getMilestone))));

            List<Project> projects = haveMissingRepos(organization) ? Collections.emptyList() :
                    mavenService.getProjects(organization, predicate);

            projects.forEach(project -> {
                project.setBranchName(gitService.getCurrentBranchName(project.getRepository().getName()));
                project.setDisable(true);
            });

            organization.getObjectContext().commitChanges();

            projectDataResponse = getProjects(predicate, agRequest);

        } else {
            List<Project> projects = projectDataResponse.getObjects().stream().filter(predicate)
                    .collect(Collectors.toList());
            for (Project project : projects) {
                project.getRootModule();

            }
            projectDataResponse.setObjects(projects);
        }

        return projectDataResponse;
    }

}
