package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.persistent.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectDTO {

    @JsonProperty("repository")
    private RepositoryDTO repository;

    @JsonProperty("rootModule")
    private ModuleDTO rootModule;

    @JsonProperty("disable")
    private boolean disabled;

    @JsonProperty("dependencies")
    private List<String> dependencies;

    @JsonProperty("branchName")
    private String branchName;

    private Boolean totally;

    public ProjectDTO() {
        dependencies = new ArrayList<>();
    }

    public RepositoryDTO getRepository() {
        return repository;
    }

    public void setTotally(Boolean totally) {
        this.totally = totally;
    }

    private void init(Project project) {
        if (project.getRootModule() != null) {
            this.rootModule = ModuleDTO.fromModel(project.getRootModule(), totally);
        }
        this.disabled = project.isDisable();
        this.repository = RepositoryDTO.fromModel(project.getRepository());
        if (this.totally) {
            for (Project dependency : project.getDependencies()) {
                dependencies.add(ProjectDTO.fromModel(dependency, totally).getRepository().getName());
            }
        }
        this.branchName = project.getBranchName();
    }

    private void convertFromDTO(Project project) {
        project.setRepository(RepositoryDTO.toModel(this.repository));
        project.setRootModule(ModuleDTO.toModel(this.rootModule));
        project.setDisable(this.disabled);
        List<Project> projectList = new ArrayList<>();
        for (String projectName : this.dependencies) {
            projectList.add(new Project(projectName));
        }
        project.addDependenciesWithoutContext(projectList);
        project.setBranchName(this.branchName);
    }

    public static ProjectDTO fromModel(Project project, Boolean totally){
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setTotally(totally);
        projectDTO.init(project);
        return projectDTO;
    }

    public static Project toModel(ProjectDTO projectDTO) {
        Project project = new Project();
        projectDTO.convertFromDTO(project);
        return project;
    }
}
