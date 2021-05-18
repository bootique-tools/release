package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._RepositoryNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public abstract class RepositoryNode extends _RepositoryNode implements Comparable<RepositoryNode> {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @JsonIgnore
    @Override
    public int getCommentsCount() {
        return super.getCommentsCount();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt);
        this.createdAtStr = dateTimeFormatter.format(createdAt);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getCreatedAtStr() {
        return super.getCreatedAtStr();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public void setCreatedAtStr(String createdAtStr) {
        super.setCreatedAtStr(createdAtStr);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "repoName")
    @Override
    public String getRepoName() {
        return super.getRepoName();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "repoName")
    @Override
    public void setRepoName(String repoName) {
        super.setRepoName(repoName);
    }

    @JsonProperty("comments")
    public void initCommentsCount(Map<String, Object> comments) {
        setCommentsCount((Integer) comments.get("totalCount"));
    }

    @Override
    public int compareTo(RepositoryNode o) {
        return getCreatedAt().compareTo(o.getCreatedAt());
    }

}
