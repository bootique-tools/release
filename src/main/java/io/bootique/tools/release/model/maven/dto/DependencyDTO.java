package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.persistent.ModuleDependency;
import io.bootique.tools.release.model.maven.persistent.Module;

public class DependencyDTO {

    @JsonProperty("type")
    protected String type;

    @JsonProperty("module")
    protected String moduleName;

    @JsonProperty("rootModule")
    protected String rootModuleName;

    private void init(ModuleDependency dependency) {
        this.type = dependency.getType();
        this.moduleName = dependency.getModule().getGithubId();
        this.rootModuleName = dependency.getRootModule().getGithubId();
    }

    private void convertFromDTO(ModuleDependency dependency) {
        dependency.setType(this.type);
        dependency.setModule(new Module(this.moduleName));
        dependency.setRootModule(new Module(this.rootModuleName));
    }

    public static DependencyDTO fromModel(ModuleDependency dependency){
        DependencyDTO dependencyDTO = new DependencyDTO();
        dependencyDTO.init(dependency);
        return dependencyDTO;
    }

    public static ModuleDependency toModel(DependencyDTO dependencyDTO) {
        ModuleDependency dependency = new ModuleDependency();
        dependencyDTO.convertFromDTO(dependency);
        return dependency;
    }
}
