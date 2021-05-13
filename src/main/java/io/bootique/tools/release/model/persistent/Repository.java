package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Repository;
import io.bootique.tools.release.service.git.GitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Repository extends _Repository implements Comparable<Repository> {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @JsonProperty("milestones")
    private Node<Milestone> milestoneNode;

    @JsonProperty("issues")
    private Node<OpenIssue> issueNode;

    @JsonProperty("pullRequests")
    private Node<PullRequest> pullRequestNode;

    public Repository() {}

    public Repository(String name) {
        super();
        super.name = name;
    }

    @JsonIgnore
    @Override
    public Organization getOrganization() {
        return super.getOrganization();
    }

    public Node<Milestone> getMilestoneNode() {
        return milestoneNode;
    }

    public void setMilestoneNodes(Node<Milestone> milestoneNodes) {
        this.milestoneNode = milestoneNodes;
    }

    public Node<OpenIssue> getIssueNode() {
        return issueNode;
    }

    public void setIssueNode(Node<OpenIssue> issueNode) {
        this.issueNode = issueNode;
    }

    public Node<PullRequest> getPullRequestNode() {
        return pullRequestNode;
    }

    public void setPullRequestNode(Node<PullRequest> pullRequestNode) {
        this.pullRequestNode = pullRequestNode;
    }

    @JsonIgnore
    public int getPrCount() {
        return super.getPullRequests().size();
    }

    @JsonIgnore
    public int getIssuesCount() {
        return super.getIssues().size();
    }

    public void addMilestone(Milestone milestone) {
        getObjectContext().registerNewObject(milestone);
        getObjectContext().commitChanges();
    }

    public int getMilestoneId(String title) {
        for (Milestone milestone : getMilestones()) {
            if (milestone.getTitle().equals(title)) {
                return milestone.getNumber();
            }
        }
        return -1;
    }

    public void closeMilestone(String title) {
        List<Milestone> milestoneList = getMilestones();
        List<Milestone> concurrentList = new CopyOnWriteArrayList<>(milestoneList);
        concurrentList.removeIf(milestone -> milestone.getTitle().equals(title));
        milestones = concurrentList; // TODO: fix this
    }

    public synchronized void renameMilestone(String title, String newTitle) {
        getMilestones().forEach(milestone -> {
            if (title.equals(milestone.getTitle())) {
                milestone.setTitle(newTitle);
            }
        });
    }

    @JsonIgnore
    public String getUpdatedAtStr() {
        return FORMATTER.format(updatedAt);
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public LocalDateTime getUpdatedAt() {
        return super.getUpdatedAt();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public LocalDateTime getPushedAt() {
        return super.getPushedAt();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setPushedAt(LocalDateTime pushedAt) {
        this.pushedAt = pushedAt;
        this.pushedAtStr = FORMATTER.format(pushedAt);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getPushedAtStr() {
        return super.getPushedAtStr();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public void setPushedAtStr(String pushedAtStr) {
        super.setPushedAtStr(pushedAtStr);
    }

    public void setLocalStatus(GitService.GitStatus localStatus) {
        super.setLStatus(localStatus.name());
    }

    public void setParent(ParentRepository parent) {
        if(this.objectContext != null) {
            super.setParent(parent);
        }
        if (parent != null) {
            super.parent = parent;
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean haveLocalRepo() {
        return !GitService.GitStatus.MISSING.toString().equals(this.lStatus);
    }

    @Override
    public int compareTo(Repository o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Repository{" + name + ", status: " + this.lStatus + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Repository repo = (Repository) o;
        return getName().equals(repo.getName());
    }

    public void addToMilestonesWithoutContext(List<Milestone> milestoneList) {
        this.milestones = milestoneList;
    }

    public List<ClosedIssue> getIssuesClose() {
        if (super.getIssuesClose() == null) {
            List<ClosedIssue> issueCloseList = new ArrayList<>();
            for (OpenIssue issue : issueNode.getNodes()) {
                issueCloseList.add(new ClosedIssue(issue));
            }
            return issueCloseList;
        }
        return super.getIssuesClose();
    }
}
