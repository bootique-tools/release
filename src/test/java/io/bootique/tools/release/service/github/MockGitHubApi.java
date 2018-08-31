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
    public Organization getOrganization(String name) {
        return getCurrentOrganization();
    }

    @Override
    public List<Milestone> getMilestones(Organization organization) {
        return null;
    }

    @Override
    public List<Issue> getIssues(Organization organization, Predicate<Issue> predicate, Comparator<Issue> comparator) {
        return null;
    }

    @Override
    public List<PullRequest> getPullRequests(Organization organization, Predicate<PullRequest> predicate, Comparator<PullRequest> comparator) {
        return null;
    }

    @Override
    public List<Repository> getRepositories(Organization organization, Predicate<Repository> predicate, Comparator<Repository> comparator) {
        return null;
    }

    @Override
    public Repository getRepository(String organizationName, String name) {
        return null;
    }

    @Override
    public Milestone createMilestone(Repository repository, String title, String description) throws IOException {
        return null;
    }

    @Override
    public void closeMilestone(Repository repository, String title, String description) {

    }

    @Override
    public void renameMilestone(Repository repository, String title, String description, String newTitle) {

    }

    @Override
    public void flushCache(Predicate<String> keyFilter) {

    }
}
