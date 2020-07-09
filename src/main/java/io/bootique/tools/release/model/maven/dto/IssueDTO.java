package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IssueDTO {

    @JsonProperty("length")
    private int length;

    public IssueDTO() { }

    public IssueDTO(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}
