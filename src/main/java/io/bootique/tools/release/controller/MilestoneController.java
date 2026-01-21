package io.bootique.tools.release.controller;

import io.agrest.DataResponse;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.github.GitHubRestAPI;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.view.MilestonesView;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

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
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Project> showAll() {
        return fetchProjects("[\"repository\",\"repository.milestones.openIssues\"," +
                "{\"path\":\"repository.milestones\",\"exp\":\"state like 'OPEN'\"}]");
    }

    @GET
    @Path("/getMilestones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMilestones(@QueryParam("selectedModules") String selectedProjectNames) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> selectedProjects = objectMapper.readValue(selectedProjectNames, List.class);
        return ObjectSelect.columnQuery(Milestone.class, Milestone.TITLE)
                .distinct()
                .where(Milestone.STATE.eq("OPEN"))
                .and(Milestone.REPOSITORY.dot(Repository.NAME).in(selectedProjects))
                .orderBy(Milestone.TITLE.asc())
                .select(cayenneRuntime.newContext());
    }

    @GET
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public String create(@QueryParam("milestoneNewTitle") String title,
                         @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = new MilestoneProcessor(title) {
            @Override
            void process(Repository repository, Milestone milestone) {
                if(milestone != null) {
                    return;
                }
                Milestone createdMilestone = gitHubRestAPI.createMilestone(repository, title);
                ObjectContext objectContext = repository.getObjectContext();
                objectContext.registerNewObject(createdMilestone);
                createdMilestone.setRepository(repository);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
        return "OK";
    }

    @GET
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    public String close(@QueryParam("milestoneTitle") String title,
                        @QueryParam("selectedModules") String selectedModules) throws IOException {
        Function<Project, String> repoProcessor = new MilestoneProcessor(title) {
            @Override
            void process(Repository repository, Milestone milestone) {
                if(milestone == null) {
                    return;
                }
                gitHubRestAPI.closeMilestone(milestone);
            }
        };
        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
        return "OK";
    }

    @GET
    @Path("/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    public String rename(@QueryParam("milestoneTitle") String title,
                         @QueryParam("selectedModules") String selectedModules,
                         @QueryParam("milestoneNewTitle") String milestoneNewTitle) throws IOException {
        Function<Project, String> repoProcessor = new MilestoneProcessor(title) {
            @Override
            void process(Repository repository, Milestone milestone) {
                if(milestone == null) {
                    return;
                }
                gitHubRestAPI.renameMilestone(milestone, milestoneNewTitle);
            }
        };

        startJob(repoProcessor, selectedModules, CONTROLLER_NAME);
        return "OK";
    }

    private abstract static class MilestoneProcessor implements Function<Project, String> {
        private final String title;

        public MilestoneProcessor(String title) {
            this.title = title;
        }

        @Override
        public String apply(Project project) {
            try {
                ObjectContext objectContext = project.getObjectContext();
                Repository repository = project.getRepository();
                Milestone milestone = ObjectSelect.query(Milestone.class)
                        .where(Milestone.REPOSITORY.eq(repository))
                        .and(Milestone.TITLE.eq(title))
                        .selectFirst(objectContext);
                process(repository, milestone);
                objectContext.commitChanges();
                return "OK";
            } catch (DesktopException ex) {
                throw new JobException(ex.getMessage(), ex);
            }
        }

        abstract void process(Repository repository, Milestone milestone);
    }
}
