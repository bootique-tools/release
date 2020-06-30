package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._RepositoryNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public abstract class RepositoryNode extends _RepositoryNode implements Comparable<RepositoryNode> {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.createdAtStr = dateTimeFormatter.format(createdAt);
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
