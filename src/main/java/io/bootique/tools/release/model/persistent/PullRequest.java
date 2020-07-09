package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._PullRequest;

public class PullRequest extends _PullRequest {

    private static final long serialVersionUID = 1L;

    public void setRepository(Repository repository) {
        this.repository = repository;
        this.repoName = repository.getName();
    }

    public User getAuthor() {
        return (User) author;
    }

    public void setAuthor(User author) {
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
