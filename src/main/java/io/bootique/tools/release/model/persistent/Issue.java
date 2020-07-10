package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Issue;

public class Issue extends _Issue {

    private static final long serialVersionUID = 1L;

    @JsonProperty("labels")
    private Node<Label> labelNode;

    public Node<Label> getLabelNode() {
        return labelNode;
    }

    public void setLabelNode(Node<Label> labelNode) {
        if (labelNode.getNodes() != null) {
            super.labels = labelNode.getNodes();
        }
        this.labelNode = labelNode;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
        this.repoName = repository.getName();
    }

    public Author getAuthor() {
        return (Author) author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "parent")
    public ParentRepository getParent() {
        return getRepository().getParent();
    }

}
