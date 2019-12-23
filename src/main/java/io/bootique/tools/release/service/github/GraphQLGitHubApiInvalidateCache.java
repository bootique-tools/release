package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.*;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.graphql.GraphQLService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.util.RequestCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;

public class GraphQLGitHubApiInvalidateCache implements GitHubApi {

    private static final URI GIT_HUB_API_URI = URI.create("https://api.github.com/graphql");

    private GraphQLService graphQLService;

    private PreferenceService preferences;

    private ContentService contentService;

    private final Map<String, String> queries = new ConcurrentHashMap<>();

    public GraphQLGitHubApiInvalidateCache(GraphQLService graphQLService, PreferenceService preferences, ContentService contentService) {
        this.graphQLService = graphQLService;
        this.preferences = preferences;
        this.contentService = contentService;
    }

    @Override
    public User getCurrentUser() {
        Viewer query = loadQuery("viewer", Collections.emptyMap(), Viewer.class);
        if(query == null) {
            return null;
        }
        return updateCache("viewer:", query.getViewer());
    }

    @Override
    public Organization getCurrentOrganization() {
        OrganizationContainer container = loadQuery("organization",
                Map.of("name", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)),
                OrganizationContainer.class);
        if(container == null) {
            return null;
        }
        Organization organization = container.getOrganization();
        organization.linkRepositories();

        return updateCache("org:" + organization.getLogin(), organization);
    }

    @Override
    public MilestoneCollection getMilestoneCollection(Repository repository) {
        String repoName = repository.getName();
        RepositoryContainer repositoryContainer = loadQuery("milestones",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repoName
                        , "totalCount", repository.getMilestoneCollection().getTotalCount()),
                RepositoryContainer.class);
        if(repositoryContainer == null) {
            return null;
        }

        return updateCache("milestones:" + repoName, repositoryContainer.getRepository().getMilestoneCollection());
    }

    @Override
    public IssueCollection getIssueCollection(Repository repo) {
        String repoName = repo.getName();
        RepositoryContainer repository = loadQuery("issues",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repoName
                        , "totalCount", repo.getIssueCollection().getTotalCount()),
                RepositoryContainer.class);
        if(repository == null) {
            return null;
        }

        return updateCache("issue:" + repoName, repository.getRepository().getIssueCollection());
    }

    @Override
    public IssueCollection getClosedIssueCollection(Repository repository, int id) {
        String repoName = repository.getName();
        int count = getClosedIssuesCount(repoName);
        RepositoryContainer repositoryContainer = loadQuery("closed-issues",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repoName
                        , "totalCount", count > 100 ? 100 : count
                        , "milestoneNumber", id),
                RepositoryContainer.class);
        if(repositoryContainer == null) {
            return null;
        }

        return updateCache("issue-closed-" + id + "-" + repoName, repositoryContainer.getRepository().getMilestone().getIssueCollection());
    }

    private int getClosedIssuesCount(String repoName) {
        RepositoryContainer repositoryContainer = loadQuery("closed-issues-count",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repoName),
                RepositoryContainer.class);
        if(repositoryContainer == null) {
            return 0;
        }
        return repositoryContainer.getRepository().getIssueCollection().getTotalCount();
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repo) {
        String repoName = repo.getName();
        RepositoryContainer repository = loadQuery("pr",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repoName
                        , "totalCount", repo.getPullRequestCollection().getTotalCount()),
                RepositoryContainer.class);
        if(repository == null) {
            return null;
        }

        return updateCache("pr:" + repoName, repository.getRepository().getPullRequestCollection());
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

    @SuppressWarnings("unchecked")
    private  <T> T updateCache(String key, T object) {
        return (T) contentService.getRepoCache().compute(key, (k, oldEntry) -> new RequestCache<>(object)).getObject();
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
}
