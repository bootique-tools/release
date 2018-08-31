package io.bootique.tools.release.model.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * "repositories": {
 *   "totalCount": 31,
 *   "nodes": []
 * }
 */
public class RepositoryCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<Repository> repositories;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }
}
