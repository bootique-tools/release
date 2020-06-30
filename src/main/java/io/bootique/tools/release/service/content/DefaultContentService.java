package io.bootique.tools.release.service.content;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.Issue;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.PullRequest;
import io.bootique.tools.release.model.persistent.PullRequestCollection;
import io.bootique.tools.release.model.persistent.Repository;
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
import javax.inject.Inject;
import javax.ws.rs.core.Configuration;

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
        if (organization == null) {
            return Collections.emptyList();
        }
        return organization.getIssues(predicates, comparator);
    }

    @Override
    public List<Milestone> getMilestones(Organization organization) {
        if (organization == null) {
            return Collections.emptyList();
        }
        for (Repository repository : organization.getRepositories()) {
            List<Milestone> milestones = gitHubApi.getMilestones(repository);
            repository.getMilestones().clear();
            repository.addToMilestones(milestones);
        }
        return organization.getMilestones();
    }

    @Override
    public List<PullRequest> getPullRequests(Organization organization, Predicate<PullRequest> predicate, Comparator<PullRequest> comparator) {
        if (organization == null) {
            return Collections.emptyList();
        }
        return organization.getPullRequests(predicate, comparator);
    }

    @Override
    public List<Repository> getRepositories(Organization organization, Predicate<Repository> predicate, Comparator<Repository> comparator) {
        if (organization == null) {
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
        if (repositories.size() != 1) {
            return null;
        }
        return repositories.get(0);
    }

    @Override
    public boolean haveCache(Configuration configuration) {
        AgRequest agRequest = Ag.request(configuration).build();
        DataResponse<Organization> organizations = Ag.select(Organization.class, configuration).request(agRequest).get();
        if (organizations.getObjects().size() == 0 ||
                organizations.getObjects().get(0).getRepositories().size() == 0) {
            return false;
        }
        return true;
    }
}
