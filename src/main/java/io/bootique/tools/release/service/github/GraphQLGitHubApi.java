package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.IssueCollection;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.MilestoneCollection;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.PullRequestCollection;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.github.User;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.util.RequestCache;

import java.util.List;
import java.util.stream.Collectors;

public class GraphQLGitHubApi implements GitHubApi {

    private PreferenceService preferences;

    private ContentService contentService;

    public GraphQLGitHubApi(PreferenceService preferences, ContentService contentService) {
        this.preferences = preferences;
        this.contentService = contentService;
    }

    @Override
    public User getCurrentUser() {
        return getFromCache("viewer:");
    }

    @Override
    public Organization getCurrentOrganization() {
        return getFromCache("org:" + preferences.get(GitHubApi.ORGANIZATION_PREFERENCE));
    }

    @Override
    public MilestoneCollection getMilestoneCollection(Repository repository) {
        MilestoneCollection milestoneCollection = getFromCache("milestones:" + repository.getName());
        if(milestoneCollection != null) {
            milestoneCollection.setMilestones(milestoneCollection.getMilestones()
                    .stream()
                    .filter(milestone -> milestone.getState() != null && milestone.getState().equalsIgnoreCase("open"))
                    .collect(Collectors.toList()));
        }
        return milestoneCollection;
    }

    public IssueCollection getIssueCollection(Repository repository) {
        return getFromCache("issue:" + repository.getName());
    }

    @Override
    public IssueCollection getClosedIssueCollection(Repository repository, int id) {
        return getFromCache("issue-closed-" + id + "-" + repository.getName());
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repo) {
        return getFromCache("pr:" + repo.getName());
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromCache(String key) {
        RequestCache cache = contentService.getRepoCache().get(key);
        return  cache != null ? (T)cache.getObject() : null;
    }
}
