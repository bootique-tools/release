package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.github.GitHubRestAPI;
import io.bootique.tools.release.service.job.JobException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("milestone")
public class MilestoneController extends BaseJobController {

    private final String CONTROLLER_NAME = "milestone";

    @Inject
    private GitHubRestAPI gitHubRestAPI;

    @GET
    public MilestonesView home() {
        return new MilestonesView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/getMilestones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMilestones(@QueryParam("selectedModules") String selectedModules) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> selectedProjects = objectMapper.readValue(selectedModules, List.class);
        AgRequest agRequest = Ag.request(configuration)
                .addInclude("[\"repository\"]")
                .andExp("[\"state like 'OPEN'\"]")
                .build();
        DataResponse<Milestone> dataResponse = Ag.select(Milestone.class, configuration).request(agRequest).get();
        List<Milestone> milestones = dataResponse.getObjects().stream().filter(milestone -> selectedProjects.contains(milestone.getRepository().getName())).collect(Collectors.toList());
        Map<String, Integer> milestoneMap = new HashMap<>();
        for (Milestone milestone : milestones) {
            if (!milestoneMap.containsKey(milestone.getTitle())) {
                milestoneMap.put(milestone.getTitle(), 1);
            } else {
                milestoneMap.put(milestone.getTitle(), milestoneMap.get(milestone.getTitle()) + 1);
            }
        }
        List<String> milestoneTitles = new ArrayList<>();
        for (String key : milestoneMap.keySet()) {
            if (milestoneMap.get(key) == selectedProjects.size()) {
                milestoneTitles.add(key);
            }
        }
        return milestoneTitles;
    }

    @GET
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(@QueryParam("milestoneNewTitle") String title,
                       @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Milestone milestone = gitHubRestAPI.createMilestone(project.getRepository(), title, "");
                project.getRepository().addMilestone(milestone);
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
                       @QueryParam("selectedModules") String selectedModules,
                       @QueryParam("milestoneNewTitle") String milestoneNewTitle) throws IOException {
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
                        "{\"path\":\"repository.milestones\",\"cayenneExp\":\"state like 'OPEN'\"}]")
                .build();
        return getProjects(project -> true, agRequest);
    }
}
