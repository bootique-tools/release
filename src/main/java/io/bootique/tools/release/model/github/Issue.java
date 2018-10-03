package io.bootique.tools.release.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * id
 * number
 * title
 * createdAt
 * author {
 *   User
 * }
 * milestone {
 *   id
 *   title
 *   number
 * }
 */
public class Issue extends RepositoryNode {

    @JsonProperty("milestone")
    private Milestone milestone;

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }
}
