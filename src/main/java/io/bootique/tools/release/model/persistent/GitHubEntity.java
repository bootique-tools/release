package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._GitHubEntity;

public abstract class GitHubEntity extends _GitHubEntity {

    private static final long serialVersionUID = 1L;

    @Override
    @JsonProperty("id")
    public String getGithubId() {
        return super.getGithubId();
    }

    @Override
    @JsonProperty("id")
    public void setGithubId(String githubId) {
        super.setGithubId(githubId);
    }
}
