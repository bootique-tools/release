package io.bootique.tools.release.model.github;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RepositoryNode extends GitHubEntity implements Comparable<RepositoryNode> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private int number;
    private String title;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createdAt;
    private User author;
    private LabelCollection labels;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "repoName")
    private String repoName;

    @JsonIgnore
    private Repository repository;

    @JsonIgnore
    private int commentsCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "createdAtStr")
    private String createdAtStr;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.createdAtStr = dateTimeFormatter.format(createdAt);
    }

    public String getCreatedAtStr() {
        return createdAtStr;
    }

    @JsonIgnore
    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
        this.repoName = repository.getName();
    }

    public String getRepoName(){
        return repoName;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LabelCollection getLabels() {
        return labels;
    }

    public void setLabels(LabelCollection labels) {
        this.labels = labels;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "parent")
    public Repository getParent() {
        return repository.getParent();
    }

    @JsonProperty("comments")
    public void initCommentsCount(Map<String, Object> comments) {
        setCommentsCount((Integer)comments.get("totalCount"));
    }

    @Override
    public int compareTo(RepositoryNode o) {
        return getCreatedAt().compareTo(o.getCreatedAt());
    }
}
