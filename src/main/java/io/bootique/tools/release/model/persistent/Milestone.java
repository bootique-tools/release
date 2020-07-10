package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Milestone;

import java.util.ArrayList;
import java.util.List;

public class Milestone extends _Milestone implements Comparable<Milestone> {

    private static final long serialVersionUID = 1L;

    public Milestone() {
        super();
        this.issues = new ArrayList<>();
    }

    @JsonProperty("issues")
    private Node<Issue> issueNode;

    @JsonProperty("issues")
    public Node<Issue> getIssueNode() {
        return issueNode;
    }

    @JsonProperty("issues")
    public void setIssueNode(Node<Issue> issueNode) {
        this.issueNode = issueNode;
    }

    @JsonProperty("issuesList")
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    @Override
    public int compareTo(Milestone o) {
        return title.compareTo(o.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Milestone milestone = (Milestone) o;
        return title.equals(milestone.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return "{milestone " + title + '}';
    }

    public void addToIssuesWithoutContext(List<Issue> issue) {
        this.issues = issue;
    }

}
