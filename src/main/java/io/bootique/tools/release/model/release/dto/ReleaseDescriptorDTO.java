package io.bootique.tools.release.model.release.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.dto.ProjectDTO;
import io.bootique.tools.release.model.maven.dto.RepositoryDTO;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;

import java.util.ArrayList;
import java.util.List;

public class ReleaseDescriptorDTO {

    @JsonProperty
    private String fromVersion;

    @JsonProperty
    private String releaseVersion;

    @JsonProperty
    private String devVersion;

    @JsonProperty
    private List<ProjectDTO> projectList;

    @JsonProperty
    private boolean autoReleaseMode;

    @JsonProperty
    private RepositoryDTO lastSuccessReleasedRepository;

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

    public RepositoryDTO getLastSuccessReleasedRepository() {
        return lastSuccessReleasedRepository;
    }

    public boolean isAutoReleaseMode() {
        return autoReleaseMode;
    }

    private void init(ReleaseDescriptor releaseDescriptor) {
        this.fromVersion = releaseDescriptor.getFromVersion();
        this.releaseVersion = releaseDescriptor.getReleaseVersion();
        this.devVersion = releaseDescriptor.getDevVersion();
        for (Project project : releaseDescriptor.getProjectList()) {
            projectList.add(ProjectDTO.fromModel(project, false));
        }
        this.autoReleaseMode = releaseDescriptor.isAutoReleaseMode();
        this.currentReleaseStage = releaseDescriptor.getCurrentReleaseStage();
        this.lastSuccessReleaseStage = releaseDescriptor.getLastSuccessReleaseStage();
        if(releaseDescriptor.getLastSuccessReleasedRepository() != null) {
            this.lastSuccessReleasedRepository = RepositoryDTO.fromModel(releaseDescriptor.getLastSuccessReleasedRepository());
        }
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
        releaseDescriptor.setLastSuccessReleasedRepository(RepositoryDTO.toModel(this.lastSuccessReleasedRepository));
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
