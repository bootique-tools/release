package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class MockGitHubApi implements GitHubApi{
    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public Organization getCurrentOrganization(){
        Organization organization = new Organization();
        organization.setName("dummy-org-00");

        Repository repository = new Repository();
        repository.setName("dummy-api");
        Repository repository1 = new Repository();
        repository1.setName("dummy-app");
        Repository repository2 = new Repository();
        repository2.setName("dummy-module1");
        RepositoryCollection repositoryCollection = new RepositoryCollection();
        repositoryCollection.setRepositories(Arrays.asList(repository, repository1, repository2));
        organization.setRepositoryCollection(repositoryCollection);
        return organization;
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
    public IssueCollection getClosedIssueCollection(Repository repository, int id) {
        return null;
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repo) {
        return null;
    }
}
