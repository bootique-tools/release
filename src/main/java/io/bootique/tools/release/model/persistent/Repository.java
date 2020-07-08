package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Repository;
import io.bootique.tools.release.service.git.GitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Repository extends _Repository implements Comparable<Repository> {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @JsonProperty("milestones")
    private MilestoneCollection milestoneCollection;

    @JsonProperty("issues")
    private IssueCollection issueCollection;

    @JsonProperty("pullRequests")
    private PullRequestCollection pullRequestCollection;

    public void addToMilestones(List<Milestone> milestones) {
        milestones.forEach(milestone -> super.addToMilestones(milestone));
    }

    @JsonIgnore
    public int getPrCount() {
        if (pullRequestCollection == null) {
            return getPullRequests().size();
        }
        return pullRequestCollection.getTotalCount();
    }

    @JsonIgnore
    public int getIssuesCount() {
        if (issueCollection == null) {
            return getIssues().size();
        }
        return this.issueCollection.getTotalCount();
    }

    public MilestoneCollection getMilestoneCollection() {
        return milestoneCollection;
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

    public IssueCollection getIssueCollection() {
        return issueCollection;
    }

    public PullRequestCollection getPullRequestCollection() {
        return pullRequestCollection;
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

    public GitService.GitStatus getLocalStatus() {
        return GitService.GitStatus.valueOf(this.lStatus);
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

    public boolean needLocalUpdate() {
        return this.lStatus == GitService.GitStatus.NEED_UPDATE.toString();
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
}
