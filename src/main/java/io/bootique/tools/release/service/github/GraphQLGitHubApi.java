package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.util.RequestCache;

import java.util.ArrayList;
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
    public PreferenceService getPreferences() {
        return preferences;
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
    public RepositoryCollection getCurrentRepositoryCollection(Organization organization) {
        return getFromCache("repositories:" + organization.getName());
    }

    @Override
    public MilestoneCollection getMilestoneCollection(Repository repository) {
        MilestoneCollection milestoneCollection = getFromCache("milestones:" + repository.getName());
        if (milestoneCollection != null) {
            milestoneCollection.setMilestones(milestoneCollection.getMilestones()
                    .stream()
                    .filter(milestone -> milestone.getState() != null && milestone.getState().equalsIgnoreCase("open"))
                    .collect(Collectors.toList()));
        }
        return milestoneCollection;
    }

    @Override
    public List<Milestone> getMilestones(Repository repository) {
        List<Milestone> milestones = new ArrayList<>();
        if (repository.getMilestones().size() > 0) {
            milestones.addAll(repository.getMilestones()
                    .stream()
                    .filter(milestone ->
                            milestone.getState() != null && milestone.getState().equalsIgnoreCase("open"))
                    .collect(Collectors.toList()));
        }
        return milestones;
    }

    public IssueCollection getIssueCollection(Repository repository) {
        return getFromCache("issue:" + repository.getName());
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repo) {
        return getFromCache("pr:" + repo.getName());
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromCache(String key) {
        RequestCache cache = contentService.getRepoCache().get(key);
        return cache != null ? (T) cache.getObject() : null;
    }
}
