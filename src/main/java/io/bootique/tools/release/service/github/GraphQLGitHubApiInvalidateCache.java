package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.graphql.GraphQLService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.util.RequestCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public PreferenceService getPreferences() {
        return preferences;
    }

    @Override
    public User getCurrentUser() {
        Viewer query = loadQuery("viewer", Collections.emptyMap(), Viewer.class);
        if (query == null) {
            return null;
        }

        return updateCache("viewer:", query.viewer);
    }

    @Override
    public Organization getCurrentOrganization() {

        OrganizationContainer container = loadQuery("organization",
                Map.of("name", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)),
                OrganizationContainer.class);
        if (container == null) {
            return null;
        }
        Organization organization = container.getOrganization();

        return updateCache("org:" + organization.getLogin(), organization);
    }

    @Override
    public RepositoryCollection getCurrentRepositoryCollection(Organization organization) {

        OrganizationContainer organizationContainer = loadQuery("repositories",
                Map.of("name", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "totalCount", organization.getRepositoryCollection().getTotalCount()),
                OrganizationContainer.class);

        if (organizationContainer == null) {
            return null;
        }

        RepositoryCollection repositoryCollection = organizationContainer.getOrganization().getRepositoryCollection();

        return updateCache("repositories:" + preferences.get(GitHubApi.ORGANIZATION_PREFERENCE), repositoryCollection);
    }

    @Override
    public MilestoneCollection getMilestoneCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("milestones",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getMilestoneCollection().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return updateCache("milestones:" + repository.getName(), repositoryContainer.getRepository().getMilestoneCollection());
    }

    @Override
    public List<Milestone> getMilestones(Repository repository) {
        return null;
    }

    @Override
    public IssueCollection getIssueCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("issues",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getIssueCollection().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return updateCache("issue:" + repository.getName(), repositoryContainer.getRepository().getIssueCollection());
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("pr",
                Map.of("owner", preferences.get(GitHubApi.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getPullRequestCollection().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return updateCache("pr:" + repository.getName(), repositoryContainer.getRepository().getPullRequestCollection());
    }

    private String getQuery(String query) {
        return queries.computeIfAbsent(query, q -> {
            try (InputStream stream = GraphQLGitHubApi.class.getResourceAsStream(q + ".query")) {
                if (stream == null) {
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
    private <T> T updateCache(String key, T object) {
        return (T) contentService.getRepoCache().compute(key, (k, oldEntry) -> new RequestCache<>(object)).getObject();
    }

    private <T> T loadQuery(String key, Map<String, Object> map, Class<T> tClass) {
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
