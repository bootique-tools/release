package io.bootique.tools.release.model.release;

import io.bootique.tools.release.model.maven.Project;

import java.util.List;

public class ReleaseDescriptor {

    private String fromVersion;
    private String releaseVersion;
    private String devVersion;
    private List<Project> projectList;
    private boolean autoReleaseMode;

    private ReleaseStage currentReleaseStage;
    private RollbackStage currentRollbackStage;

    public ReleaseDescriptor(){}

    public ReleaseDescriptor(String fromVersion,
                             String releaseVersion,
                             String devVersion,
                             List<Project> projectList,
                             ReleaseStage releaseStage,
                             RollbackStage rollbackStage,
                             boolean autoReleaseMode) {
        this.fromVersion = fromVersion;
        this.releaseVersion = releaseVersion;
        this.devVersion = devVersion;
        this.projectList = projectList;
        this.currentReleaseStage = releaseStage;
        this.currentRollbackStage = rollbackStage;
        this.autoReleaseMode = autoReleaseMode;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getDevVersion() {
        return devVersion;
    }

    public ReleaseStage getCurrentReleaseStage() { return currentReleaseStage; }

    public void setCurrentReleaseStage(ReleaseStage currentReleaseStage) { this.currentReleaseStage = currentReleaseStage; }

    public void setDevVersion(String devVersion) {
        this.devVersion = devVersion;
    }

    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public boolean isAutoReleaseMode() {
        return autoReleaseMode;
    }

    public void setAutoReleaseMode(boolean autoReleaseMode) {
        this.autoReleaseMode = autoReleaseMode;
    }

    public RollbackStage getCurrentRollbackStage() {
        return currentRollbackStage;
    }

    public void setCurrentRollbackStage(RollbackStage currentRollbackStage) {
        this.currentRollbackStage = currentRollbackStage;
    }
}
