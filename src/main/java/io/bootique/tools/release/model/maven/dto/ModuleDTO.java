package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.persistent.Dependency;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;

import java.util.ArrayList;
import java.util.List;

public class ModuleDTO {

    @JsonProperty("group")
    private String group;

    @JsonProperty("id")
    private String id;

    @JsonProperty("version")
    private String version;

    @JsonProperty("dependencies")
    private List<DependencyDTO> dependencies;

    public ModuleDTO() {
        dependencies = new ArrayList<>();
    }

    private void init(Module module) {
        this.group = module.getGroupStr();
        this.id = module.getId();
        this.version = module.getVersion();
        for (Dependency dependency : module.getDependencies()) {
            dependencies.add(DependencyDTO.fromModel(dependency));
        }
    }

    private void convertFromDTO(Module module) {
        module.setGroupStr(this.group);
        module.setId(this.id);
        module.setVersion(this.version);
        List<Dependency> dependencyList = new ArrayList<>();
        for (DependencyDTO dependencyDTO : this.dependencies) {
            dependencyList.add(DependencyDTO.toModel(dependencyDTO));
        }
        module.addDependenciesWithoutContext(dependencyList);
    }

    public static ModuleDTO fromModel(Module module){
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.init(module);
        return moduleDTO;
    }

    public static Module toModel(ModuleDTO moduleDTO) {
        Module module = new Module();
        moduleDTO.convertFromDTO(module);
        return module;
    }
}
