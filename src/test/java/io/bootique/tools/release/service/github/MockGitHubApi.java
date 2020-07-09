package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.ObjectContext;

import java.util.Arrays;

public class MockGitHubApi implements GitHubApiImport{

    private ObjectContext context;

    public MockGitHubApi(ObjectContext context) {
        this.context = context;
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public PreferenceService getPreferences() {
        return null;
    }

    @Override
    public Organization getCurrentOrganization(){
        Organization organization = context.newObject(Organization.class);
        organization.setName("dummy-org-00");

        Repository repository = context.newObject(Repository.class);
        repository.setName("dummy-api");
        Repository repository1 = context.newObject(Repository.class);
        repository1.setName("dummy-app");
        Repository repository2 = context.newObject(Repository.class);
        repository2.setName("dummy-module1");
        RepositoryCollection repositoryCollection = new RepositoryCollection();
        repositoryCollection.setRepositories(Arrays.asList(repository, repository1, repository2));
        organization.setRepositoryCollection(repositoryCollection);
        organization.addToRepositories(repository);
        organization.addToRepositories(repository1);
        organization.addToRepositories(repository2);

        return organization;
    }

    @Override
    public RepositoryCollection getCurrentRepositoryCollection(Organization organization) {
        return null;
    }

    @Override
    public MilestoneCollection getMilestoneCollection(Repository repository) {
        return null;
    }

    @Override
    public IssueCollection getIssueCollection(Repository repository) {
        return null;
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repo) {
        return null;
    }
}
