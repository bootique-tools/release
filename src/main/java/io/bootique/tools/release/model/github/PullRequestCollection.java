package io.bootique.tools.release.model.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PullRequestCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<PullRequest> pullRequests;

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
