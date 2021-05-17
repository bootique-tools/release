package io.bootique.tools.release.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import io.bootique.job.BaseJob;
import io.bootique.job.JobMetadata;
import io.bootique.job.runnable.JobResult;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.ModuleDependency;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.maven.MavenService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenProjectsImport extends BaseJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubDataImportJob.class);

    @Inject
    Provider<ServerRuntime> cayenneRuntimeProvider;

    @Inject
    MavenService mavenService;

    @Inject
    GitService gitService;

    public MavenProjectsImport() {
        super(JobMetadata.build(MavenProjectsImport.class));
    }

    @Override
    public JobResult run(Map<String, Object> map) {
        LOGGER.info("Run Maven projects import job.");

        ObjectContext context = cayenneRuntimeProvider.get().newContext();

        List<Repository> repositories = ObjectSelect.query(Repository.class).select(context);
        if(repositories.isEmpty()) {
            LOGGER.info("No repositories yet, return.");
            return JobResult.success(getMetadata());
        }

        int updatedCount = 0;
        List<Project> createdProjects = new ArrayList<>();
        for(Repository repo : repositories) {
            if(!mavenService.isMavenProject(repo)) {
                continue;
            }

            Project project = ObjectSelect.query(Project.class).where(Project.REPOSITORY.eq(repo)).selectFirst(context);
            if(project == null) {
                // TODO: this job should also be update to update projects in case their modules are changed
                project = mavenService.createProject(repo);
                createdProjects.add(project);
            }

            project.setBranchName(gitService.getCurrentBranchName(repo.getName()));
            project.setDisable(true);
            updatedCount++;
        }
        context.commitChanges();

        linkProjects(createdProjects);
        context.commitChanges();

        LOGGER.info("Job done, created {}, updated {} projects.", createdProjects.size(), updatedCount);

        return JobResult.success(getMetadata());
    }

    private void linkProjects(List<Project> createdProjects) {
        for(Project createdProject: createdProjects) {
            for (Module module : createdProject.getModules()) {
                for (ModuleDependency dependency : module.getDependencies()) {
                    Project depProject = dependency.getModule().getProject();
                    if (depProject != null && !createdProject.equals(depProject)) {
                        createdProject.addToDependencies(depProject);
                    }
                }
            }
        }
    }
}
