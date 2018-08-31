package io.bootique.tools.release.model.github;

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

    private Milestone milestone;

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }
}
