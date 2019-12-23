package io.bootique.tools.release.job;

import io.bootique.job.BaseJob;
import io.bootique.job.JobMetadata;
import io.bootique.job.runnable.JobResult;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.github.GitHubApi;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

public class QueryJob extends BaseJob {

    @Inject
    @Named("updateCache")
    private GitHubApi gitHubApi;

    public QueryJob() {
        super(JobMetadata.build(QueryJob.class));
    }

    @Override
    public JobResult run(Map<String, Object> map) {
        GitHubApi gitHubApi = this.gitHubApi;
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
