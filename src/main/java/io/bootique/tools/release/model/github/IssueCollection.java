package io.bootique.tools.release.model.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IssueCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<Issue> issues;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}
