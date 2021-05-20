package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._OpenIssue;

public class OpenIssue extends _OpenIssue {

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
        if(getObjectContext() == null) {
            this.milestone = milestone;
        } else {
            super.setMilestone(milestone);
        }
    }

    public void setRepository(Repository repository) {
        if(getObjectContext() == null) {
            this.repository = repository;
        } else {
            super.setRepository(repository);
        }
        if(repository != null) {
            this.repoName = repository.getName(); // TODO: remove this
        }
    }

    public void setAuthor(Author author) {
        if(getObjectContext() == null) {
            this.author = author;
        } else {
            super.setAuthor(author);
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "parent")
    public Repository getParent() {
        return getRepository().getParent();
    }

}
