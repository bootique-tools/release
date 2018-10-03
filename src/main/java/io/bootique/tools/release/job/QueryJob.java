package io.bootique.tools.release.job;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.bootique.job.BaseJob;
import io.bootique.job.JobMetadata;
import io.bootique.job.runnable.JobResult;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.github.GitHubApi;

import java.util.Map;

public class QueryJob extends BaseJob {

    @Inject
    @Named("updateCache")
    private GitHubApi gitHubApi;

    public QueryJob() {
        super(JobMetadata.build(QueryJob.class));
    }

    @Override
    public JobResult run(Map<String, Object> map) {
        gitHubApi.getCurrentUser();
        Organization organization = gitHubApi.getCurrentOrganization();
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            gitHubApi.getIssueCollection(repository);
            gitHubApi.getMilestoneCollection(repository);
            gitHubApi.getPullRequestCollection(repository);
        }

        return JobResult.success(getMetadata());
    }
}
