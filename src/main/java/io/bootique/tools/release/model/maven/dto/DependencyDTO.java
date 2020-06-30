package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.maven.persistent.Dependency;
import io.bootique.tools.release.model.maven.persistent.Module;

public class DependencyDTO {

    @JsonProperty("type")
    protected String type;

    @JsonProperty("module")
    protected String moduleName;

    @JsonProperty("rootModule")
    protected String rootModuleName;

    private void init(Dependency dependency) {
        this.type = dependency.getType();
        this.moduleName = dependency.getModule().getId();
        this.rootModuleName = dependency.getRootModule().getId();
    }

    private void convertFromDTO(Dependency dependency) {
        dependency.setType(this.type);
        dependency.setModule(new Module(this.moduleName));
        dependency.setRootModule(new Module(this.rootModuleName));
    }

    public static DependencyDTO fromModel(Dependency dependency){
        DependencyDTO dependencyDTO = new DependencyDTO();
        dependencyDTO.init(dependency);
        return dependencyDTO;
    }

    public static Dependency toModel(DependencyDTO dependencyDTO) {
        Dependency dependency = new Dependency();
        dependencyDTO.convertFromDTO(dependency);
        return dependency;
    }
}
