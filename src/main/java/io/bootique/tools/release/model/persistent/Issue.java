package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Issue;

import java.util.List;

public class Issue extends _Issue {

    private static final long serialVersionUID = 1L;

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

    public void setLabels(LabelCollection labels) {
        this.labels = labels.getLabels();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "parent")
    public ParentRepository getParent() {
        return getRepository().getParent();
    }

}
