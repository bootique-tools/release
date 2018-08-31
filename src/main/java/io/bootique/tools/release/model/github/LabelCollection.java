package io.bootique.tools.release.model.github;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LabelCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<Label> labels;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }
}
