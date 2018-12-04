package io.bootique.tools.release.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class MilestoneCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<Milestone> milestones;

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
    }
}
