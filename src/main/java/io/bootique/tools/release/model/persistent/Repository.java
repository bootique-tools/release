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

    public Repository() {}

    public Repository(String name) {
        super();
        super.name = name;
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @JsonProperty("milestones")
    private Node<Milestone> milestoneNode;

    public Node<Milestone> getMilestoneNode() {
        return milestoneNode;
    }

    public void setMilestoneNodes(Node<Milestone> milestoneNodes) {
        this.milestoneNode = milestoneNodes;
    }

    @JsonProperty("issues")
    private Node<IssueOpen> issueNode;

    public Node<IssueOpen> getIssueNode() {
        return issueNode;
    }

    public void setIssueNode(Node<IssueOpen> issueNode) {
        this.issueNode = issueNode;
    }

    @JsonProperty("pullRequests")
    private Node<PullRequest> pullRequestNode;

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

        for (Milestone milestone : concurrentList) {
            if (milestone.getTitle().equals(title)) {
                concurrentList.remove(milestone);
            }
        }
        milestones = concurrentList;
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
        return dateTimeFormatter.format(updatedAt);
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPushedAt(LocalDateTime pushedAt) {
        this.pushedAt = pushedAt;
        this.pushedAtStr = dateTimeFormatter.format(pushedAt);
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
        return this.lStatus != GitService.GitStatus.MISSING.toString();
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

    public List<IssueClose> getIssuesClose() {
        if (super.getIssuesClose() == null) {
            List<IssueClose> issueCloseList = new ArrayList<>();
            for (IssueOpen issue : issueNode.getNodes()) {
                issueCloseList.add(new IssueClose(issue));
            }
            return issueCloseList;
        }
        return super.getIssuesClose();
    }
}
