package io.bootique.tools.release.model.release;

import java.util.Map;

public class RepositoryDescriptor {

    private String repositoryName;
    private Map<ReleaseStage, ReleaseStageStatus> stageStatusMap;
    private String centralDeploymentId;

    public RepositoryDescriptor(String repositoryName, Map<ReleaseStage, ReleaseStageStatus> stages) {
        this.repositoryName = repositoryName;
        this.stageStatusMap = stages;
    }

    public RepositoryDescriptor() {
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Map<ReleaseStage, ReleaseStageStatus> getStageStatusMap() {
        return stageStatusMap;
    }

    public void setStageStatusMap(Map<ReleaseStage, ReleaseStageStatus> stageStatusMap) {
        this.stageStatusMap = stageStatusMap;
    }

    public void setCentralDeploymentId(String centralDeploymentId) {
        this.centralDeploymentId = centralDeploymentId;
    }

    public String getCentralDeploymentId() {
        return centralDeploymentId;
    }
}
