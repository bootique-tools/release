package io.bootique.tools.release.model.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.service.git.GitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {
 *   "id": "MDEwOlJlcG9zaXRvcnk0Nzc2Nzk2Nw==",
 *   "name": "bootique",
 *   "url": "https://github.com/bootique/bootique",
 *   "sshUrl": "git@github.com:bootique/bootique.git",
 *   "parent": {..},
 *   "pullRequests": {
 *     "totalCount": 0,
 *     "nodes": [
 *        {..},
 *        {..}
 *     ]
 *   },
 *   "issues": {
 *     "totalCount": 29,
 *     "nodes": [
 *        {..},
 *        {..}
 *     ]
 *   },
 *   "milestones": {
 *
 *   }
 * }
 */
public class Repository extends GitHubEntity implements Comparable<Repository> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private String name;
    private String sshUrl;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime updatedAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime pushedAt;

    @JsonProperty("description")
    private String description;

    private Repository parent;

    @JsonIgnore
    private Organization organization;

    @JsonProperty("milestones")
    private MilestoneCollection milestoneCollection;

    private Milestone milestone;

    @JsonProperty("issues")
    private IssueCollection issueCollection;

    @JsonProperty("pullRequests")
    private PullRequestCollection pullRequestCollection;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String pushedAtStr;

    private GitService.GitStatus localStatus = GitService.GitStatus.MISSING;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSshUrl() {
        return sshUrl;
    }

    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    @JsonIgnore
    public int getPrCount() {
        return pullRequestCollection.getTotalCount();
    }

    @JsonIgnore
    public int getIssuesCount() {
        return issueCollection.getTotalCount();
    }

    public MilestoneCollection getMilestoneCollection() {
        return milestoneCollection;
    }

    public void setMilestoneCollection(MilestoneCollection milestoneCollection) {
        this.milestoneCollection = milestoneCollection;
    }

    public void addMilestoneToCollection(Milestone milestone) {
        milestoneCollection.addMilestone(milestone);
    }

    public int getMilestoneId(String title) {
        for(Milestone milestone : milestoneCollection.getMilestones()) {
            if(milestone.getTitle().equals(title)){
                return milestone.getNumber();
            }
        }
        return -1;
    }

    public void closeMilestone(String title) {
        List<Milestone> milestoneList = milestoneCollection.getMilestones();
        List<Milestone> concurrentList = new CopyOnWriteArrayList<>(milestoneList);

        for(Milestone milestone : concurrentList) {
            if(milestone.getTitle().equals(title)) {
                concurrentList.remove(milestone);
            }
        }
        milestoneCollection.setMilestones(concurrentList);
    }

    public synchronized void renameMilestone(String title, String newTitle) {
        milestoneCollection.getMilestones().forEach(milestone -> {
            if(title.equals(milestone.getTitle())) {
                milestone.setTitle(newTitle);
            }
        });
    }

    public Repository getParent() {
        return parent;
    }

    public void setParent(Repository parent) {
        this.parent = parent;
    }

    public IssueCollection getIssueCollection() {
        return issueCollection;
    }

    public void setIssueCollection(IssueCollection issueCollection) {
        this.issueCollection = issueCollection;
    }

    public PullRequestCollection getPullRequestCollection() {
        return pullRequestCollection;
    }

    public void setPullRequestCollection(PullRequestCollection pullRequestCollection) {
        this.pullRequestCollection = pullRequestCollection;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonIgnore
    public String getUpdatedAtStr() {
        return dateTimeFormatter.format(updatedAt);
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getPushedAt() {
        return pushedAt;
    }

    public String getPushedAtStr() {
        return pushedAtStr;
    }

    public void setPushedAt(LocalDateTime pushedAt) {
        this.pushedAt = pushedAt;
        this.pushedAtStr = dateTimeFormatter.format(pushedAt);
    }

    public GitService.GitStatus getLocalStatus() {
        return localStatus;
    }

    public void setLocalStatus(GitService.GitStatus localStatus) {
        this.localStatus = localStatus;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean haveLocalRepo() {
        return localStatus != GitService.GitStatus.MISSING;
    }

    public boolean needLocalUpdate() {
        return localStatus == GitService.GitStatus.NEED_UPDATE;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(Repository o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Repository{" + name + ", status: " + localStatus + '}';
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }
}
