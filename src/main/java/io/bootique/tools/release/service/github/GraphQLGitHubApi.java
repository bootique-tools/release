package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.IssueCollection;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.MilestoneCollection;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.OrganizationContainer;
import io.bootique.tools.release.model.github.PullRequest;
import io.bootique.tools.release.model.github.PullRequestCollection;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.github.RepositoryContainer;
import io.bootique.tools.release.model.github.User;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.graphql.GraphQLService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GraphQLGitHubApi implements GitHubApi {

    private static final URI GIT_HUB_API_URI = URI.create("https://api.github.com/graphql");

    @Inject
    private GraphQLService graphQLService;

    @Inject
    private PreferenceService preferences;

    @Inject
    private GitHubRestAPI gitHubRestAPI;

    @Inject
    private GitService gitService;

    private final Map<String, RequestCache<?>> repoCache = new ConcurrentHashMap<>();
    private final Map<String, String> queries = new ConcurrentHashMap<>();

    @Override
    public User getCurrentUser() {
        return getFromCache("viewer:", () -> {
            Viewer query = loadQuery("viewer", Collections.emptyMap(), Viewer.class);
            if(query == null) {
                return null;
            }
            return query.getViewer();
        });
    }

    @Override
    public Organization getCurrentOrganization() {
        return getOrganization(preferences.get(GitHubApi.ORGANIZATION_PREFERENCE));
    }

    @Override
    public Organization getOrganization(String name) {
        return getFromCache("org:" + name, () -> {
            OrganizationContainer container = loadQuery("organization",
                    Map.of("name", name),
                    OrganizationContainer.class);
            if(container == null) {
                return null;
            }
            Organization organization = container.getOrganization();
            organization.linkRepositories();
            return organization;
        });
    }

    @Override
    public List<Milestone> getMilestones(Organization organization) {
        if(organization == null) {
            return Collections.emptyList();
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            MilestoneCollection milestoneCollection = getMilestoneCollection(repository.getName(), repository.getMilestoneCollection().getTotalCount());
            repository.setMilestoneCollection(milestoneCollection);
        }
        organization.setMilestonesRepo();
        return organization.getMilestones();
    }

    private MilestoneCollection getMilestoneCollection(String repoName, int totalCount) {
        return getFromCache("milestones:" + repoName, () -> {
            RepositoryContainer repository = loadQuery("milestones",
                    Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                            , "name", repoName
                            , "totalCount", totalCount),
                    RepositoryContainer.class);
            if(repository == null) {
                return null;
            }
            return repository.getRepository().getMilestoneCollection();
        });
    }

    @Override
    public List<Issue> getIssues(Organization organization, Predicate<Issue> predicate, Comparator<Issue> comparator) {
        if(organization == null) {
            return Collections.emptyList();
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            IssueCollection issueCollection = getIssueCollection(repository.getName(), repository.getIssueCollection().getTotalCount());
            repository.setIssueCollection(issueCollection);
        }
        organization.setIssuesRepo();

        return organization.getIssues(predicate, comparator);
    }

    private IssueCollection getIssueCollection(String repoName, int totalCount) {
        return getFromCache("issue:" + repoName, () -> {
            RepositoryContainer repository = loadQuery("issues",
                    Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                            , "name", repoName
                            , "totalCount", totalCount),
                    RepositoryContainer.class);
            if(repository == null) {
                return null;
            }
            return repository.getRepository().getIssueCollection();
        });
    }

    @Override
    public List<PullRequest> getPullRequests(Organization organization, Predicate<PullRequest> predicate, Comparator<PullRequest> comparator) {
        if(organization == null) {
            return Collections.emptyList();
        }
        for(Repository repository : organization.getRepositoryCollection().getRepositories()) {
            PullRequestCollection pullRequestCollection = getPrCollection(repository.getName(), repository.getPullRequestCollection().getTotalCount());
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
            if (preferences.have(GitService.BASE_PATH_PREFERENCE)) {
                r.setLocalStatus(gitService.status(r));
            }
        });
        return repositoryList;
    }

    private PullRequestCollection getPrCollection(String repoName, int totalCount) {
        return getFromCache("pr:" + repoName, () -> {
            RepositoryContainer repository = loadQuery("pr",
                    Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                            , "name", repoName
                            , "totalCount", totalCount),
                    RepositoryContainer.class);
            if(repository == null) {
                return null;
            }
            return repository.getRepository().getPullRequestCollection();
        });
    }

    @Override
    public Repository getRepository(String organizationName, String name) {
        Organization organization = getOrganization(organizationName);
        List<Repository> repositories = organization
                .getRepositories(repo -> repo.getName().equals(name), Repository::compareTo);
        if(repositories.size() != 1) {
            return null;
        }
        return repositories.get(0);
    }

    @Override
    public Milestone createMilestone(Repository repository, String title, String description) throws IOException {
        return gitHubRestAPI.createMilestone(repository, title, description);
    }

    @Override
    public void closeMilestone(Repository repository, String title, String description) {
        gitHubRestAPI.patchMilestone(repository, title, description, "closed", repository.getMilestoneId(title));
    }

    @Override
    public void renameMilestone(Repository repository, String title, String description, String newTitle) {
        gitHubRestAPI.patchMilestone(repository, newTitle, description, "open", repository.getMilestoneId(title));
    }

    private String getQuery(String query) {
        return queries.computeIfAbsent(query, q -> {
            try (InputStream stream = GraphQLGitHubApi.class.getResourceAsStream(q + ".query")) {
                if(stream == null) {
                    throw new RuntimeException("Query not found: " + q);
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void flushOrgCache() {
        flushCache(key -> key.startsWith("org:"));
    }

    public void flushViewerCache() {
        flushCache("viewer:"::equals);
    }

    @Override
    public void flushCache(Predicate<String> keyFilter) {
        repoCache.entrySet().removeIf(entry -> keyFilter.test(entry.getKey()));
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromCache(String key, Supplier<T> supplier) {
        return (T)repoCache.compute(key, (k, oldEntry) -> {
            if(oldEntry != null && oldEntry.isValid()) {
                return oldEntry;
            }
            T object = supplier.get();
            return new RequestCache<>(object);
        }).getObject();
    }

    private<T> T loadQuery(String key, Map<String, Object> map, Class<T> tClass) {
        return graphQLService.query(tClass,
                GIT_HUB_API_URI,
                preferences.get(AUTH_TOKEN_PREFERENCE),
                getQuery(key),
                map);
    }

    /**
     * Value object to deserialize viewer query result
     */
    private static class Viewer {
        private User viewer;
        private User getViewer() {
            return viewer;
        }
        public void setViewer(User viewer) {
            this.viewer = viewer;
        }
    }

    /**
     * API query cache
     */
    private static final class RequestCache<T> {

        private static final long TIMEOUT = 5 * 60 * 1000;

        private final long creationTime;
        private final T object;

        private RequestCache(T object) {
            this.creationTime = System.currentTimeMillis();
            this.object = object;
        }

        private boolean isValid() {
            return (System.currentTimeMillis() - creationTime) < TIMEOUT;
        }

        private T getObject() {
            return object;
        }
    }

    public void setGraphQLService(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    public void setPreferences(PreferenceService preferences) {
        this.preferences = preferences;
    }
}
