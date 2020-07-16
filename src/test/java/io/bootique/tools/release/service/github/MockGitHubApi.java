package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.ObjectContext;

import java.util.Arrays;
import java.util.List;

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
        Node<Repository> repositoryNode = new Node<>();
        repositoryNode.setNodes(Arrays.asList(repository, repository1, repository2));
        repositoryNode.setTotalCount(repositoryNode.getNodes().size());
        organization.setRepositoryNode(repositoryNode);
        organization.addToRepositories(repository);
        organization.addToRepositories(repository1);
        organization.addToRepositories(repository2);

        return organization;
    }

    @Override
    public List<Repository> getCurrentRepositoryCollection(Organization organization) {
        return null;
    }

    @Override
    public List<Milestone> getMilestoneCollection(Repository repository) {
        return null;
    }

    @Override
    public List<IssueOpen> getIssueCollection(Repository repository) {
        return null;
    }

    @Override
    public List<IssueClose> getClosedIssueCollection(Repository repository) {
        return null;
    }

    @Override
    public List<PullRequest> getPullRequestCollection(Repository repo) {
        return null;
    }
}
