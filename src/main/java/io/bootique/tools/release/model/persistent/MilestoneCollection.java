package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MilestoneCollection {

    private int totalCount;

    @JsonProperty("nodes")
    private List<Milestone> milestones;

    public MilestoneCollection() {
    }

    public MilestoneCollection(int totalCount, List<Milestone> milestones) {
        this.totalCount = totalCount;
        this.milestones = milestones;
    }

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
