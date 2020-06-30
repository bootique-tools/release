package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IssueDTO {

    @JsonProperty("length")
    private int length;

    public IssueDTO() { }

    public int getLength() {
        return length;
    }

    public IssueDTO(int length) {
        this.length = length;
    }
}
