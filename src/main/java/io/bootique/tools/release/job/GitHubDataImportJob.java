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

public class GitHubDataImportJob extends BaseJob {

    @Inject
    @Named("updateCache")
    private GitHubApi gitHubApi;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private GitService gitService;

    private Provider<ServerRuntime> runtimeProvider;

    private Boolean update;

    public GitHubDataImportJob() {
        super(JobMetadata.build(GitHubDataImportJob.class));
    }

    @Inject
    public GitHubDataImportJob(Provider<ServerRuntime> runtimeProvider) {
        super(JobMetadata.build(GitHubDataImportJob.class));
        this.runtimeProvider = runtimeProvider;
        this.update = false;
    }

    @Override
    public JobResult run(Map<String, Object> map) {

        ServerRuntime runtime = runtimeProvider.get();
        ObjectContext context = runtime.newContext();
        GitHubApi gitHubApi = this.gitHubApi;

        if (this.update) {
            getCurrents(context);
        } else {
            List<Organization> organizations = ObjectSelect.query(Organization.class)
                    .where(Organization.LOGIN.eq(gitHubApi.getPreferences().get(GitHubApi.ORGANIZATION_PREFERENCE))).select(context);

            if (organizations.size() == 0) {
                getCurrents(context);
            }
        }

        this.update = true;
        return JobResult.success(getMetadata());
    }

    private void getCurrents(ObjectContext objectContext) {

        deleteAll(objectContext);

        User user = gitHubApi.getCurrentUser();
        user.setObjectContext(objectContext);
        objectContext.registerNewObject(user);

        Organization organization = gitHubApi.getCurrentOrganization();

        getRepositories(objectContext, organization);

        if (organization.getObjectId().getIdSnapshot().get("ID") == null) {
            for (Repository repository : organization.getRepositoryCollection().getRepositories()) {
                if (preferenceService.have(GitService.BASE_PATH_PREFERENCE)) {
                    repository.setLocalStatus(gitService.status(repository));
                }
                Map<String, Milestone> milestoneMap = new HashMap<>();
                Map<String, Label> labelMap = new HashMap<>();

                getMilestones(objectContext, repository, milestoneMap);
                getIssues(objectContext, repository, milestoneMap, labelMap);
                getPRs(objectContext, repository, labelMap);

                objectContext.commitChanges();
                milestoneMap.clear();
                labelMap.clear();
            }
        }
    }

    private void getRepositories(ObjectContext context, Organization organization) {
        RepositoryCollection repositoryCollection = gitHubApi.getCurrentRepositoryCollection(organization);

        for (Repository repository : repositoryCollection.getRepositories()) {
            if (repository.getParent() != null) {
                context.registerNewObject(repository.getParent());
            }
            context.registerNewObject(repository);
        }
        organization.setRepositoryCollection(repositoryCollection);
        organization.linkRepositories();
    }

    private void getIssues(ObjectContext context, Repository repository, Map<String, Milestone> milestoneMap, Map<String, Label> labelMap) {
        IssueCollection issueCollection = gitHubApi.getIssueCollection(repository);

        for (Issue issue : issueCollection.getIssues()) {
            if (issue.getMilestone() != null) {
                if (milestoneMap.containsKey(issue.getMilestone().getGithubId())) {
                    issue.setMilestone(milestoneMap.get(issue.getMilestone().getGithubId()));
                } else {
                    context.registerNewObject(issue.getMilestone());
                }
            }
            context.registerNewObject(issue.getAuthor());
            LabelCollection labels = new LabelCollection();
            for (Label label : issue.getLabels()) {
                if (!labelMap.containsKey(label.getGithubId())) {
                    context.registerNewObject(label);
                    labelMap.put(label.getGithubId(), label);
                }
                labels.getLabels().add(labelMap.get(label.getGithubId()));
            }
            issue.setLabels(labels);
            issue.setRepository(repository);
            repository.addToIssues(issue);
            context.registerNewObject(issue);
        }
    }

    private void getMilestones(ObjectContext context, Repository repository, Map<String, Milestone> milestoneMap) {
        MilestoneCollection milestoneCollection = gitHubApi.getMilestoneCollection(repository);

        for (Milestone milestone : milestoneCollection.getMilestones()) {
            milestone.setRepository(repository);
            if (milestone.getIssues() != null || milestone.getIssues().size() > 0) {
                for (Issue issue : milestone.getIssues()) {
                    context.registerNewObject(issue);
                }
            }
            repository.addToMilestones(milestone);
            context.registerNewObject(milestone);
            milestoneMap.put(milestone.getGithubId(), milestone);
        }
    }

    private void getPRs(ObjectContext context, Repository repository, Map<String, Label> labelMap) {
        PullRequestCollection pullRequestCollection = gitHubApi.getPullRequestCollection(repository);

        for (PullRequest pullRequest : pullRequestCollection.getPullRequests()) {
            LabelCollection labels = new LabelCollection();
            for (Label label : pullRequest.getLabels()) {
                if (!labelMap.containsKey(label.getGithubId())) {
                    context.registerNewObject(label);
                    labelMap.put(label.getGithubId(), label);
                }
                labels.getLabels().add(labelMap.get(label.getGithubId()));
            }
            pullRequest.setLabels(labels);
            pullRequest.setRepository(repository);
            repository.addToPullRequests(pullRequest);
            context.registerNewObject(pullRequest);
        }
    }

    private void deleteAll(ObjectContext objectContext) {
        SQLExec.query("delete from Label").update(objectContext);
        SQLExec.query("delete from Issue").update(objectContext);
        SQLExec.query("delete from Milestone").update(objectContext);
        SQLExec.query("delete from PullRequest").update(objectContext);
        SQLExec.query("delete from ModuleDependency").update(objectContext);
        SQLExec.query("delete from Module").update(objectContext);
        SQLExec.query("delete from ProjectDependency").update(objectContext);
        SQLExec.query("delete from Project").update(objectContext);
        SQLExec.query("delete from ParentRepository").update(objectContext);
        SQLExec.query("delete from Repository").update(objectContext);
        SQLExec.query("delete from Organization").update(objectContext);
        SQLExec.query("delete from User").update(objectContext);
    }
}
