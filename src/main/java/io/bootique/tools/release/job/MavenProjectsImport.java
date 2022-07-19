package io.bootique.tools.release.job;

import io.bootique.job.BaseJob;
import io.bootique.job.JobMetadata;
import io.bootique.job.runnable.JobResult;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.maven.MavenService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MavenProjectsImport extends BaseJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectsImport.class);

    @Inject
    Provider<ServerRuntime> cayenneRuntimeProvider;

    @Inject
    MavenService mavenService;

    public MavenProjectsImport() {
        super(JobMetadata.build(MavenProjectsImport.class));
    }

    @Override
    public JobResult run(Map<String, Object> map) {
        LOGGER.info("Run Maven projects import job.");

        ObjectContext context = cayenneRuntimeProvider.get().newContext();

        List<Repository> repositories = ObjectSelect.query(Repository.class)
                .where(Repository.UPSTREAM.isFalse())
                .select(context);
        if (repositories.isEmpty()) {
            LOGGER.info("No repositories yet, return.");
            return JobResult.success(getMetadata());
        }

        // sync Maven projects with repositories
        List<Project> createdProjects = syncProjects(repositories);
        context.commitChanges();

        syncDependencies(createdProjects);
        context.commitChanges();

        LOGGER.info("Job done, created {} projects.", createdProjects.size());

        return JobResult.success(getMetadata());
    }

    private void syncDependencies(List<Project> projects) {
        projects.forEach(mavenService::syncDependencies);
    }

    private List<Project> syncProjects(List<Repository> repositories) {
        return repositories.stream()
                .filter(mavenService::isMavenProject)
                .map(mavenService::createOrUpdateProject)
                .collect(Collectors.toList());
    }
}
