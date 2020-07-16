package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.persistent.ModuleDependency;
import io.bootique.tools.release.model.maven.persistent.Module;

import java.util.ArrayList;
import java.util.List;

public class ModuleDTO {

    @JsonProperty("group")
    private String group;

    @JsonProperty("githubId")
    private String githubId;

    @JsonProperty("version")
    private String version;

    @JsonProperty("dependencies")
    private List<DependencyDTO> dependencies;

    private Boolean totally;

    public ModuleDTO() {
        dependencies = new ArrayList<>();
    }

    public void setTotally(Boolean totally) {
        this.totally = totally;
    }

    private void init(Module module) {
        this.group = module.getGroupStr();
        this.githubId = module.getGithubId();
        this.version = module.getVersion();
        if (this.totally) {
            for (ModuleDependency dependency : module.getDependencies()) {
                dependencies.add(DependencyDTO.fromModel(dependency));
            }
        }
    }

    private void convertFromDTO(Module module) {
        module.setGroupStr(this.group);
        module.setGithubId(this.githubId);
        module.setVersion(this.version);
        List<ModuleDependency> dependencyList = new ArrayList<>();
        for (DependencyDTO dependencyDTO : this.dependencies) {
            dependencyList.add(DependencyDTO.toModel(dependencyDTO));
        }
        module.addDependenciesWithoutContext(dependencyList);
    }

    public static ModuleDTO fromModel(Module module, Boolean totally){
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setTotally(totally);
        moduleDTO.init(module);
        return moduleDTO;
    }

    public static Module toModel(ModuleDTO moduleDTO) {
        Module module = new Module();
        moduleDTO.convertFromDTO(module);
        return module;
    }
}
