package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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

    public RepositoryCollection() {
    }

    public RepositoryCollection(int totalCount, List<Repository> repositories) {
        this.totalCount = totalCount;
        this.repositories = repositories;
    }

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