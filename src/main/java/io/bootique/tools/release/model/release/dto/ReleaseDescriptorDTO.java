package io.bootique.tools.release.model.release.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.dto.ProjectDTO;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;

import java.util.ArrayList;
import java.util.List;

public class ReleaseDescriptorDTO {

    @JsonProperty("fromVersion")
    private String fromVersion;

    @JsonProperty("releaseVersion")
    private String releaseVersion;

    @JsonProperty("devVersion")
    private String devVersion;

    @JsonProperty("projectList")
    private List<ProjectDTO> projectList;

    @JsonProperty("autoReleaseMode")
    private boolean autoReleaseMode;

    private ReleaseStage currentReleaseStage;

    private ReleaseStage lastSuccessReleaseStage;

    public ReleaseDescriptorDTO() {
        this.projectList = new ArrayList<>();
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public List<ProjectDTO> getProjectList() {
        return projectList;
    }

    public ReleaseStage getCurrentReleaseStage() {
        return currentReleaseStage;
    }

    public ReleaseStage getLastSuccessReleaseStage() {
        return lastSuccessReleaseStage;
    }

    public boolean isAutoReleaseMode() {
        return autoReleaseMode;
    }

    private void init(ReleaseDescriptor releaseDescriptor) {
        this.fromVersion = releaseDescriptor.getFromVersion();
        this.releaseVersion = releaseDescriptor.getReleaseVersion();
        this.devVersion = releaseDescriptor.getDevVersion();
        for (Project project : releaseDescriptor.getProjectList()) {
            projectList.add(ProjectDTO.fromModel(project));
        }
        this.autoReleaseMode = releaseDescriptor.isAutoReleaseMode();
        this.currentReleaseStage = releaseDescriptor.getCurrentReleaseStage();
        this.lastSuccessReleaseStage = releaseDescriptor.getLastSuccessReleaseStage();
    }

    private void convertFromDTO(ReleaseDescriptor releaseDescriptor) {
        releaseDescriptor.setFromVersion(this.fromVersion);
        releaseDescriptor.setReleaseVersion(this.releaseVersion);
        releaseDescriptor.setDevVersion(this.devVersion);
        List<Project> projectList = new ArrayList<>();
        for (ProjectDTO projectDTO : this.projectList) {
            projectList.add(ProjectDTO.toModel(projectDTO));
        }
        releaseDescriptor.setProjectList(projectList);
        releaseDescriptor.setAutoReleaseMode(this.autoReleaseMode);
        releaseDescriptor.setCurrentReleaseStage(this.currentReleaseStage);
        releaseDescriptor.setLastSuccessReleaseStage(this.lastSuccessReleaseStage);
    }

    public static ReleaseDescriptorDTO fromModel(ReleaseDescriptor releaseDescriptor) {
        ReleaseDescriptorDTO releaseDescriptorDTO = new ReleaseDescriptorDTO();
        releaseDescriptorDTO.init(releaseDescriptor);
        return releaseDescriptorDTO;
    }

    public static ReleaseDescriptor toModel(ReleaseDescriptorDTO releaseDescriptorDTO) {
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptorDTO.convertFromDTO(releaseDescriptor);
        return releaseDescriptor;
    }
}
