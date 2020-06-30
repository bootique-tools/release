package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.dto.ReleaseDescriptorDTO;

import java.util.ArrayList;
import java.util.List;

public class ProjectDTO {

    @JsonProperty("repository")
    private RepositoryDTO repository;

    @JsonProperty("rootModule")
    private ModuleDTO rootModule;

    @JsonProperty("disable")
    private boolean disable;

    @JsonProperty("dependencies")
    private List<ProjectDTO> dependencies;

    @JsonProperty("branchName")
    private String branchName;

    public ProjectDTO() {
        dependencies = new ArrayList<>();
    }

    public RepositoryDTO getRepository() {
        return repository;
    }

    private void init(Project project) {
        this.repository = RepositoryDTO.fromModel(project.getRepository());
        this.rootModule = ModuleDTO.fromModel(project.getRootModule());
        this.disable = project.isDisable();
        for (Project dependency : project.getDependencies()) {
            dependencies.add(ProjectDTO.fromModel(dependency));
        }
        this.branchName = project.getBranchName();
    }

    private void convertFromDTO(Project project) {
        project.setRepository(RepositoryDTO.toModel(this.repository));
        project.setRootModule(ModuleDTO.toModel(this.rootModule));
        project.setDisable(this.disable);
        List<Project> projectList = new ArrayList<>();
        for (ProjectDTO projectDTO : this.dependencies) {
            projectList.add(ProjectDTO.toModel(projectDTO));
        }
        project.addDependenciesWithoutContext(projectList);
        project.setBranchName(this.branchName);
    }

    public static ProjectDTO fromModel(Project project){
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.init(project);
        return projectDTO;
    }

    public static Project toModel(ProjectDTO projectDTO) {
        Project project = new Project();
        projectDTO.convertFromDTO(project);
        return project;
    }
}
