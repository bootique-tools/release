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
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.reflect.ClassDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

public class GitHubDataImportJob extends BaseJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubDataImportJob.class);

    @Inject
    private GitHubApiImport gitHubApiImport;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private GitService gitService;

    private Provider<ServerRuntime> runtimeProvider;

    @SuppressWarnings("unused")
    public GitHubDataImportJob() {
        super(JobMetadata.build(GitHubDataImportJob.class));
    }

    @Inject
    public GitHubDataImportJob(Provider<ServerRuntime> runtimeProvider) {
        super(JobMetadata.build(GitHubDataImportJob.class));
        this.runtimeProvider = runtimeProvider;
    }

    @Override
    public JobResult run(Map<String, Object> map) {
        ServerRuntime runtime = runtimeProvider.get();

        ObjectContext context = runtime.newContext();
        syncGitHubDataData(context);
        context.commitChanges();

        return JobResult.success(getMetadata());
    }

    private void syncGitHubDataData(ObjectContext context) {
        LOGGER.info("Running GitHub data update ...");

        Organization organization = syncOrganization(context, preferenceService.get(GitHubApiImport.ORGANIZATION_PREFERENCE));
        List<Repository> repositories = syncRepositories(context, organization);
        for(Repository repository: repositories) {
            syncMilestones(context, repository);
            syncOpenIssues(context, repository);
            syncClosedIssues(context, repository);
            syncPullRequests(context, repository);
        }
    }

    private Organization syncOrganization(ObjectContext context, String organizationName) {
        Organization organization = ObjectSelect.query(Organization.class, Organization.LOGIN.eq(organizationName))
                .selectFirst(context);
        if(organization != null) {
            // TODO: should we sync data?
            return organization;
        }

        User user = gitHubApiImport.getCurrentUser();
        user.setObjectContext(context);
        context.registerNewObject(user);

        organization = gitHubApiImport.getCurrentOrganization();
        context.registerNewObject(organization);

        return organization;
    }

    private List<Repository> syncRepositories(ObjectContext context, Organization organization) {
        List<Repository> repositoriesIn = gitHubApiImport.getCurrentRepositoryCollection(organization);
        List<Repository> repositoriesOut = new ArrayList<>();
        for(Repository repo : repositoriesIn) {
            Repository syncedRepo = syncEntity(context, Repository.class, repo);
            syncedRepo.setOrganization(organization);
            repositoriesOut.add(syncedRepo);

            if (repo.getParent() != null) {
                Repository parent = syncEntity(context, Repository.class, repo.getParent());
                parent.setUpstream(true);
                syncedRepo.setParent(parent);
            }

            if (preferenceService.have(GitService.BASE_PATH_PREFERENCE)) {
                syncedRepo.setLocalStatus(gitService.status(syncedRepo));
            }
        }
        return repositoriesOut;
    }

    private void syncMilestones(ObjectContext context, Repository repository) {
        List<Milestone> milestones = gitHubApiImport.getMilestoneCollection(repository);
        for (Milestone next : milestones) {
            Milestone milestone = syncEntity(context, Milestone.class, next);
            milestone.setRepository(repository);
        }
    }

    private void syncOpenIssues(ObjectContext context, Repository repository) {
        List<OpenIssue> issues = gitHubApiImport.getIssueCollection(repository);
        for(OpenIssue next : issues) {
            OpenIssue issue = syncEntity(context, OpenIssue.class, next);
            issue.setRepository(repository);
        }
    }

    private void syncClosedIssues(ObjectContext context, Repository repository) {
        List<ClosedIssue> issues = gitHubApiImport.getClosedIssueCollection(repository);
        for(ClosedIssue next : issues) {
            ClosedIssue issue = syncEntity(context, ClosedIssue.class, next);
            issue.setRepository(repository);
        }
    }

    private void syncPullRequests(ObjectContext context, Repository repository) {
        List<PullRequest> pullRequests = gitHubApiImport.getPullRequestCollection(repository);
        for (PullRequest next : pullRequests) {
            PullRequest pr = syncEntity(context, PullRequest.class, next);
            pr.setRepository(repository);
        }
    }

    static <T extends GitHubEntity> T syncEntity(ObjectContext context, Class<T> entityType, T entityFrom) {
        T entityTo = findEntity(context, entityType, entityFrom.getGithubId());
        boolean fromDb = true;
        if(entityTo == null) {
            entityTo = entityFrom;
            fromDb = false;
        }

        syncProperties(context, entityFrom, entityTo, fromDb);
        if(!fromDb) {
            context.registerNewObject(entityTo);
        }
        return entityTo;
    }

    @SuppressWarnings("unchecked")
    static <T extends GitHubEntity> T findEntity(ObjectContext context, Class<T> entityType, String githubId) {
        // lookup in the DB
        T entityFromDb = ObjectSelect.query(entityType, GitHubEntity.GITHUB_ID.eq(githubId)).selectOne(context);
        if(entityFromDb != null) {
            return entityFromDb;
        }

        // search for the uncommitted entity
        for (Object next : context.newObjects()) {
            if (entityType.isInstance(next) && githubId.equals(((T) next).getGithubId())) {
                return (T) next;
            }
        }
        return null;
    }

    static void syncProperties(ObjectContext context, GitHubEntity entityFrom, GitHubEntity entityTo, boolean fromDb) {
        EntityResolver entityResolver = context.getEntityResolver();
        String entityName = entityResolver.getObjEntity(entityTo).getName();
        ClassDescriptor descriptor = entityResolver.getClassDescriptor(entityName);
        descriptor.visitAllProperties(new MergingAttributeVisitor(context, entityFrom, entityTo, fromDb));
    }

}
