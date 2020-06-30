package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PullRequestCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<PullRequest> pullRequests;

    public PullRequestCollection() {
    }

    public PullRequestCollection(int totalCount, List<PullRequest> pullRequests) {
        this.totalCount = totalCount;
        this.pullRequests = pullRequests;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(List<PullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }
}