package io.bootique.tools.release.service.content;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.IssueCollection;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.MilestoneCollection;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.PullRequest;
import io.bootique.tools.release.model.github.PullRequestCollection;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.util.RequestCache;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultContentService implements ContentService {

    @Inject
    private GitHubApi gitHubApi;

    @Inject
    private GitService gitService;

    @Inject
    private PreferenceService preferenceService;

    private final Map<String, RequestCache<?>> repoCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, RequestCache<?>> getRepoCache() {
        return repoCache;
    }

    @Override
    public List<Issue> getIssues(Organization organization, List<Predicate<Issue>> predicates, Comparator<Issue> comparator) {
        if(organization == null) {
            return Collections.emptyList();
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            IssueCollection issueCollection = gitHubApi.getIssueCollection(repository);
            repository.setIssueCollection(issueCollection);
        }
        organization.setIssuesRepo();

        return organization.getIssues(predicates, comparator);
    }

    @Override
    public List<Milestone> getMilestones(Organization organization) {
        if(organization == null) {
            return Collections.emptyList();
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            MilestoneCollection milestoneCollection = gitHubApi.getMilestoneCollection(repository);
            repository.setMilestoneCollection(milestoneCollection);
        }
        organization.setMilestonesRepo();
        return organization.getMilestones();
    }

    @Override
    public List<PullRequest> getPullRequests(Organization organization, Predicate<PullRequest> predicate, Comparator<PullRequest> comparator) {
        if(organization == null) {
            return Collections.emptyList();
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            PullRequestCollection pullRequestCollection = gitHubApi.getPullRequestCollection(repository);
            repository.setPullRequestCollection(pullRequestCollection);
        }
        organization.setPRsRepo();

        return organization.getPullRequests(predicate, comparator);
    }

    @Override
    public List<Repository> getRepositories(Organization organization, Predicate<Repository> predicate, Comparator<Repository> comparator) {
        if(organization == null) {
            return Collections.emptyList();
        }
        List<Repository> repositoryList = organization.getRepositories(predicate, comparator);
        repositoryList.forEach(r -> {
            if (preferenceService.have(GitService.BASE_PATH_PREFERENCE)) {
                r.setLocalStatus(gitService.status(r));
            }
        });
        return repositoryList;
    }

    @Override
    public Repository getRepository(String organizationName, String name) {
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Repository> repositories = organization
                .getRepositories(repo -> repo.getName().equals(name), Repository::compareTo);
        if(repositories.size() != 1) {
            return null;
        }
        return repositories.get(0);
    }

    @Override
    public boolean haveCache() {
        Organization organization = gitHubApi.getCurrentOrganization();
        if(organization == null) {
            return false;
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            String repoName = repository.getName();
            if(repoCache.get("milestones:" + repoName) == null ||
                    repoCache.get("issue:" + repoName) == null ||
                    repoCache.get("pr:" + repoName) == null) {
                return false;
            }
        }

        return true;
    }
}
