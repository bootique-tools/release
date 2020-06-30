package io.bootique.tools.release.job;

import io.bootique.job.BaseJob;
import io.bootique.job.JobMetadata;
import io.bootique.job.runnable.JobResult;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLExec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class QueryJob extends BaseJob {

    @Inject
    @Named("updateCache")
    private GitHubApi gitHubApi;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private GitService gitService;

    private Provider<ServerRuntime> runtimeProvider;

    public QueryJob() {
        super(JobMetadata.build(QueryJob.class));
    }

    @Inject
    public QueryJob(Provider<ServerRuntime> runtimeProvider) {
        super(JobMetadata.build(QueryJob.class));
        this.runtimeProvider = runtimeProvider;
    }

    @Override
    public JobResult run(Map<String, Object> map) {

        ServerRuntime runtime = runtimeProvider.get();
        ObjectContext context = runtime.newContext();
        GitHubApi gitHubApi = this.gitHubApi;

        if (this.gitHubApi.isUpdate()) {
            getCurrents(context);
        } else {
            List<Organization> organizations = ObjectSelect.query(Organization.class)
                    .where(Organization.LOGIN.eq(gitHubApi.getPreferences().get(GitHubApi.ORGANIZATION_PREFERENCE))).select(context);

            if (organizations.size() == 0) {
                getCurrents(context);
            }
        }

        gitHubApi.setUpdate(true);
        return JobResult.success(getMetadata());
    }

    private void getCurrents(ObjectContext objectContext) {

        deleteAll(objectContext);

        User user = gitHubApi.getCurrentUser();
        user.setObjectContext(objectContext);
        objectContext.registerNewObject(user);

        Organization organization = gitHubApi.getCurrentOrganization();

        getRepositories(objectContext, organization);

        if (organization.getObjectId().getIdSnapshot().get("ID_PK") == null) {
            for (Repository repository : organization.getRepositoryCollection().getRepositories()) {
                if (preferenceService.have(GitService.BASE_PATH_PREFERENCE)) {
                    repository.setLocalStatus(gitService.status(repository));
                }
                Map<String, Milestone> milestoneMap = new HashMap<>();

                getMilestones(objectContext, repository, milestoneMap);
                getIssues(objectContext, repository, milestoneMap);
                getPRs(objectContext, repository);

                objectContext.commitChanges();
                milestoneMap.clear();
            }
        }
    }

    private void getRepositories(ObjectContext context, Organization organization) {
        RepositoryCollection repositoryCollection = gitHubApi.getCurrentRepositoryCollection(organization);

        for (Repository repository : repositoryCollection.getRepositories()) {
            repository.setObjectContext(context);
            context.registerNewObject(repository);
        }
        organization.setRepositoryCollection(repositoryCollection);
        organization.linkRepositories();
    }

    private void getIssues(ObjectContext context, Repository repository, Map<String, Milestone> milestoneMap) {
        IssueCollection issueCollection = gitHubApi.getIssueCollection(repository);

        for (Issue issue : issueCollection.getIssues()) {
            issue.setObjectContext(context);
            if (issue.getMilestone() != null) {
                if (milestoneMap.containsKey(issue.getMilestone().getId())) {
                    issue.setMilestone(milestoneMap.get(issue.getMilestone().getId()));
                } else {
                    issue.getMilestone().setObjectContext(context);
                    context.registerNewObject(issue.getMilestone());
                }
            }
            issue.getAuthor().setObjectContext(context);
            context.registerNewObject(issue.getAuthor());
            for (Label label : issue.getLabels()) {
                label.setObjectContext(context);
                context.registerNewObject(label);
            }
            issue.setRepository(repository);
            repository.addToIssues(issue);
            context.registerNewObject(issue);
        }
    }

    private void getMilestones(ObjectContext context, Repository repository, Map<String, Milestone> milestoneMap) {
        MilestoneCollection milestoneCollection = gitHubApi.getMilestoneCollection(repository);

        for (Milestone milestone : milestoneCollection.getMilestones()) {
            milestone.setRepository(repository);
            milestone.setObjectContext(context);
            if (milestone.getIssues() != null || milestone.getIssues().size() > 0) {
                for (Issue issue : milestone.getIssues()) {
                    issue.setObjectContext(context);
                    context.registerNewObject(issue);
                }
            }
            repository.addToMilestones(milestone);
            context.registerNewObject(milestone);
            milestoneMap.put(milestone.getId(), milestone);
        }
    }

    private void getPRs(ObjectContext context, Repository repository) {
        PullRequestCollection pullRequestCollection = gitHubApi.getPullRequestCollection(repository);

        for (PullRequest pullRequest : pullRequestCollection.getPullRequests()) {
            pullRequest.setObjectContext(context);
            context.registerNewObject(pullRequest);
        }
    }

    private void deleteAll(ObjectContext objectContext) {
        SQLExec.query("delete from github.Label").update(objectContext);
        SQLExec.query("delete from github.Issue").update(objectContext);
        SQLExec.query("delete from github.Milestone").update(objectContext);
        SQLExec.query("delete from github.PullRequest").update(objectContext);
        SQLExec.query("delete from github.Dependency").update(objectContext);
        SQLExec.query("delete from github.Module").update(objectContext);
        SQLExec.query("delete from github.Project").update(objectContext);
        SQLExec.query("delete from github.Repository").update(objectContext);
        SQLExec.query("delete from github.Organization").update(objectContext);
        SQLExec.query("delete from github.User").update(objectContext);
    }
}
