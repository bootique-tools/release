package io.bootique.tools.release.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.view.MilestonesView;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Path("milestone")
public class MilestoneController extends BaseController {

    @Inject
    private GitHubApi gitHubApi;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private MavenService mavenService;

    @Inject
    private BatchJobService jobService;

    @GET
    public MilestonesView home() {
        return new MilestonesView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization());
    }

    @GET
    @Path("/getMilestones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMilestones(@QueryParam("selectedModules") String selectedModules) throws IOException {
        List selectedProjects = objectMapper.readValue(selectedModules, List.class);
        List<Project> projects = getProjects(project -> selectedProjects.contains(project.getRepository().getName()));
        Map<String, Integer> milestoneMap = new HashMap<>();
        for(Project project : projects) {
            for(Milestone milestone : project.getRepository().getMilestoneCollection().getMilestones()) {
                if(!milestoneMap.containsKey(milestone.getTitle())) {
                    milestoneMap.put(milestone.getTitle(), 1);
                } else {
                    milestoneMap.put(milestone.getTitle(), milestoneMap.get(milestone.getTitle()) + 1);
                }
            }
        }
        List<String> milestones = new ArrayList<>();
        for(String key : milestoneMap.keySet()) {
            if(milestoneMap.get(key) == projects.size()) {
                milestones.add(key);
            }
        }
        return milestones;
    }

    @GET
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void create(@QueryParam("milestoneNewTitle") String title,
                       @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Milestone milestone = gitHubApi.createMilestone(project.getRepository(), title, "");;
                project.getRepository().addMilestoneToCollection(milestone);
                return "";
            } catch (IOException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules);
    }

    @GET
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void close(@QueryParam("milestoneTitle") String title,
                      @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                gitHubApi.closeMilestone(project.getRepository(), title, "");
                repository.closeMilestone(title);
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules);
    }

    @GET
    @Path("/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void rename(@QueryParam("milestoneTitle") String title,
                      @QueryParam("selectedModules") String selectedModules, @QueryParam("milestoneNewTitle") String milestoneNewTitle) throws IOException {
        Function<Project, String> repoProcessor = project -> {
            try {
                Repository repository = project.getRepository();
                gitHubApi.renameMilestone(project.getRepository(), title, "", milestoneNewTitle);
                repository.renameMilestone(title, milestoneNewTitle);
                return "";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        };
        startJob(repoProcessor, selectedModules);
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> showAll() {
        return getProjects(project -> true);
    }

    private void startJob(Function<Project, String> proc, String selectedModules) throws IOException {
        List<Project> allProjects = getSelectedProjects(selectedModules);
        BatchJobDescriptor<Project, String> descriptor = new BatchJobDescriptor<>(allProjects, proc);
        preferences.set(BatchJobService.CURRENT_JOB_ID, jobService.submit(descriptor).getId());
    }

    private List<Project> getProjects(Predicate<Project> predicate) {
        Organization organization = gitHubApi.getCurrentOrganization();
        gitHubApi.getMilestones(organization).forEach(milestone ->
                milestone.setIssues(
                        gitHubApi.getIssues(organization, issue -> milestone.equals(issue.getMilestone()),
                                Comparator.comparing(Issue::getMilestone))));
        return haveMissingRepos(organization) ? new ArrayList<>() :
                mavenService.getProjects(organization, predicate);
    }
}
