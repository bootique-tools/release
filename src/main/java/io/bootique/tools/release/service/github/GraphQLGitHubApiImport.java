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

public class GraphQLGitHubApiImport implements GitHubApiImport {

    private static final URI GIT_HUB_API_URI = URI.create("https://api.github.com/graphql");

    private GraphQLService graphQLService;

    private PreferenceService preferences;

    private final Map<String, String> queries = new ConcurrentHashMap<>();

    public GraphQLGitHubApiImport(GraphQLService graphQLService, PreferenceService preferences) {
        this.graphQLService = graphQLService;
        this.preferences = preferences;
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

        return container.getOrganization();
    }

    @Override
    public List<Repository> getCurrentRepositoryCollection(Organization organization) {

        OrganizationContainer organizationContainer = loadQuery("repositories",
                Map.of("name", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "totalCount", organization.getRepositoryNode().getTotalCount()),
                OrganizationContainer.class);

        if (organizationContainer == null) {
            return null;
        }

        return organizationContainer.getOrganization().getRepositoryNode().getNodes();
    }

    @Override
    public List<Milestone> getMilestoneCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("milestones",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getMilestoneNode().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getMilestoneNode().getNodes();
    }

    @Override
    public List<IssueOpen> getIssueCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("issues",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getIssueNode().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getIssueNode().getNodes();
    }

    @Override
    public List<IssueClose> getClosedIssueCollection(Repository repository) {
        String repoName = repository.getName();
        int count = getClosedIssuesCount(repoName);
        RepositoryContainer repositoryContainer = loadQuery("closed-issues",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repoName
                        , "totalCount", Math.min(count, 100)),
                RepositoryContainer.class);
        if(repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getIssuesClose();
    }

    private int getClosedIssuesCount(String repoName) {
        RepositoryContainer repositoryContainer = loadQuery("closed-issues-count",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repoName),
                RepositoryContainer.class);
        if(repositoryContainer == null) {
            return 0;
        }
        return repositoryContainer.getRepository().getIssueNode().getTotalCount();
    }

    @Override
    public List<PullRequest> getPullRequestCollection(Repository repository) {

        RepositoryContainer repositoryContainer = loadQuery("pr",
                Map.of("owner", preferences.get(GitHubApiImport.ORGANIZATION_PREFERENCE)
                        , "name", repository.getName()
                        , "totalCount", repository.getPullRequestNode().getTotalCount()),
                RepositoryContainer.class);
        if (repositoryContainer == null) {
            return null;
        }

        return repositoryContainer.getRepository().getPullRequestNode().getNodes();
    }

    private String getQuery(String query) {
        return queries.computeIfAbsent(query, q -> {
            try (InputStream stream = GraphQLGitHubApiImport.class.getResourceAsStream(q + ".query")) {
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
