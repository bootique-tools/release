package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Milestone;

public class Milestone extends _Milestone implements Comparable<Milestone> {

    private static final long serialVersionUID = 1L;

    @JsonProperty("issues")
    private Node<OpenIssue> issueNode;

    @JsonProperty("issues")
    public Node<OpenIssue> getIssueNode() {
        return issueNode;
    }

    @JsonProperty("issues")
    public void setIssueNode(Node<OpenIssue> issueNode) {
        this.issueNode = issueNode;
    }

    @JsonIgnore
    @Override
    public Repository getRepository() {
        return super.getRepository();
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

}
