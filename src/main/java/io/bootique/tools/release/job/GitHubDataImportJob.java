package io.bootique.tools.release.job;

import io.bootique.job.BaseJob;
import io.bootique.job.JobMetadata;
import io.bootique.job.runnable.JobResult;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApiImport;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLExec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

public class GitHubDataImportJob extends BaseJob {

    @Inject
    private GitHubApiImport gitHubApiImport;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private GitService gitService;

    private Provider<ServerRuntime> runtimeProvider;

    private Boolean update;

    private Map<String, Author> authorMap;

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
        GitHubApiImport gitHubApi = this.gitHubApiImport;

        if (this.update) {
            getCurrents(context);
        } else {
            List<Organization> organizations = ObjectSelect.query(Organization.class)
                    .where(Organization.LOGIN.eq(gitHubApi.getPreferences().get(GitHubApiImport.ORGANIZATION_PREFERENCE))).select(context);

            if (organizations.size() == 0) {
                getCurrents(context);
            }
        }

        this.update = true;
        return JobResult.success(getMetadata());
    }

    private void getCurrents(ObjectContext objectContext) {

        deleteAll(objectContext);

        User user = gitHubApiImport.getCurrentUser();
        user.setObjectContext(objectContext);
        objectContext.registerNewObject(user);

        Organization organization = gitHubApiImport.getCurrentOrganization();

        getRepositories(objectContext, organization);

        authorMap = new HashMap<>();
        if (organization.getObjectId().getIdSnapshot().get("ID") == null) {
            for (Repository repository : organization.getRepositories()) {
                if (preferenceService.have(GitService.BASE_PATH_PREFERENCE)) {
                    repository.setLocalStatus(gitService.status(repository));
                }
                Map<String, Milestone> milestoneMap = new HashMap<>();
                Map<String, Label> labelMap = new HashMap<>();

                getMilestones(objectContext, repository, milestoneMap);
                getIssues(objectContext, repository, milestoneMap);
                getPRs(objectContext, repository);

                objectContext.commitChanges();
                milestoneMap.clear();
                labelMap.clear();
            }
        }
        authorMap.clear();
    }

    private void getRepositories(ObjectContext context, Organization organization) {
        List<Repository> repositories = gitHubApiImport.getCurrentRepositoryCollection(organization);

        for (Repository repository : repositories) {
            if (repository.getParent() != null) {
                context.registerNewObject(repository.getParent());
            }
            context.registerNewObject(repository);
            organization.addToRepositories(repository);
        }
    }

    private void getIssues(ObjectContext context, Repository repository, Map<String, Milestone> milestoneMap) {
        List<IssueOpen> issues = gitHubApiImport.getIssueCollection(repository);

        for (IssueOpen issue : issues) {
            if (issue.getMilestone() != null) {
                if (milestoneMap.containsKey(issue.getMilestone().getGithubId())) {
                    issue.setMilestone(milestoneMap.get(issue.getMilestone().getGithubId()));
                } else {
                    context.registerNewObject(issue.getMilestone());
                }
            }
            if (authorMap.containsKey(issue.getAuthor().getGithubId())) {
                issue.setAuthor(authorMap.get(issue.getAuthor().getGithubId()));
            } else {
                context.registerNewObject(issue.getAuthor());
                authorMap.put(issue.getAuthor().getGithubId(), issue.getAuthor());
            }
            for (Label label : issue.getLabels()) {
                context.registerNewObject(label);
            }
            issue.setRepository(repository);
            repository.addToIssues(issue);
            context.registerNewObject(issue);
        }

        List<IssueClose> issuesClosed = gitHubApiImport.getClosedIssueCollection(repository);

        for (IssueClose issueClose : issuesClosed) {
            if (issueClose.getMilestone() != null) {
                if (milestoneMap.containsKey(issueClose.getMilestone().getGithubId())) {
                    issueClose.setMilestone(milestoneMap.get(issueClose.getMilestone().getGithubId()));
                } else {
                    context.registerNewObject(issueClose.getMilestone());
                }
            }
            issueClose.setRepository(repository);
            repository.addToIssuesClose(issueClose);
            context.registerNewObject(issueClose);
        }
    }

    private void getMilestones(ObjectContext context, Repository repository, Map<String, Milestone> milestoneMap) {
        List<Milestone> milestones = gitHubApiImport.getMilestoneCollection(repository);

        for (Milestone milestone : milestones) {
            milestone.setRepository(repository);
            if (milestone.getIssues() != null || milestone.getIssues().size() > 0) {
                for (IssueOpen issue : milestone.getIssues()) {
                    context.registerNewObject(issue);
                }
            }
            repository.addToMilestones(milestone);
            context.registerNewObject(milestone);
            milestoneMap.put(milestone.getGithubId(), milestone);
        }
    }

    private void getPRs(ObjectContext context, Repository repository) {
        List<PullRequest> pullRequests = gitHubApiImport.getPullRequestCollection(repository);

        for (PullRequest pullRequest : pullRequests) {
            for (Label label : pullRequest.getLabels()) {
                context.registerNewObject(label);
            }
            if (authorMap.containsKey(pullRequest.getAuthor().getGithubId())) {
                pullRequest.setAuthor(authorMap.get(pullRequest.getAuthor().getGithubId()));
            } else {
                context.registerNewObject(pullRequest.getAuthor());
                authorMap.put(pullRequest.getAuthor().getGithubId(), pullRequest.getAuthor());
            }
            pullRequest.setRepository(repository);
            repository.addToPullRequests(pullRequest);
            context.registerNewObject(pullRequest);
        }
    }

    private void deleteAll(ObjectContext objectContext) {
        SQLExec.query("delete from Label").update(objectContext);
        SQLExec.query("delete from IssueOpen").update(objectContext);
        SQLExec.query("delete from IssueClose").update(objectContext);
        SQLExec.query("delete from Milestone").update(objectContext);
        SQLExec.query("delete from PullRequest").update(objectContext);
        SQLExec.query("delete from ModuleDependency").update(objectContext);
        SQLExec.query("delete from Module").update(objectContext);
        SQLExec.query("delete from ProjectDependency").update(objectContext);
        SQLExec.query("delete from Project").update(objectContext);
        SQLExec.query("delete from Repository").update(objectContext);
        SQLExec.query("delete from ParentRepository").update(objectContext);
        SQLExec.query("delete from Organization").update(objectContext);
        SQLExec.query("delete from Author").update(objectContext);
        SQLExec.query("delete from User").update(objectContext);
    }
}
