package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
