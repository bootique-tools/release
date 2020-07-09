package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.graphql.GraphQLService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraphQLGitHubApiImportInvalidateCache implements GitHubApiImport {

    private static final URI GIT_HUB_API_URI = URI.create("https://api.github.com/graphql");

    private GraphQLService graphQLService;

    private PreferenceService preferences;

    private final Map<String, String> queries = new ConcurrentHashMap<>();

    public GraphQLGitHubApiImportInvalidateCache(GraphQLService graphQLService, PreferenceService preferences) {
        this.graphQLService = graphQLService;
        this.preferences = preferences;
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

        return query.getViewer();
    }

    @Override
    public Organization getCurrentOrganization() {

        OrganizationContainer container = loadQuery("organization",
                Map.of("name", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)),
                OrganizationContainer.class);
        if (container == null) {
            return null;
        }
        Organization organization = container.getOrganization();

        return organization;
    }

    @Override
    public RepositoryCollection getCurrentRepositoryCollection(Organization organization) {

        OrganizationContainer organizationContainer = loadQuery("repositories",
                Map.of("name", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "totalCount", organization.getRepositoryCollection().getTotalCount()),
                OrganizationContainer.class);

        if (organizationContainer == null) {
            return null;
        }

        RepositoryCollection repositoryCollection = organizationContainer.getOrganization().getRepositoryCollection();

        return repositoryCollection;
    }

    @Override
    public MilestoneCollection getMilestoneCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("milestones",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getMilestoneCollection().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getMilestoneCollection();
    }

    @Override
    public IssueCollection getIssueCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("issues",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getIssueCollection().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getIssueCollection();
    }

    @Override
    public PullRequestCollection getPullRequestCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("pr",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getPullRequestCollection().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getPullRequestCollection();
    }

    private String getQuery(String query) {
        return queries.computeIfAbsent(query, q -> {
            try (InputStream stream = GraphQLGitHubApiImportInvalidateCache.class.getResourceAsStream(q + ".query")) {
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
