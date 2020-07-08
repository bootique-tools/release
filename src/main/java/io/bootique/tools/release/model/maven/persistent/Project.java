package io.bootique.tools.release.model.maven.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bootique.tools.release.model.maven.persistent.auto._Project;
import io.bootique.tools.release.model.persistent.Repository;

import java.nio.file.Path;
import java.util.*;

public class Project extends _Project implements Comparable<Project> {

    private static final long serialVersionUID = 1L;

    private Path path;

    public Project() {
        super();
    }

    public Project(Repository repository, Path path, Module rootModule) {
        super();
        this.repository = Objects.requireNonNull(repository);
        this.path = path;
        this.pathStr = path.toString();
        this.rootModule = rootModule;
        this.version = rootModule.getVersion();
        this.modules = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        repository.getObjectContext().registerNewObject(this);
        rootModule.setProject(this);
        rootModule.setRootModule(this);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        this.pathStr = path.toString();
    }

    public void setRootModule(Module rootModule) {
        if (rootModule.getObjectContext() != null) {
            super.setRootModule(rootModule);
        } else {
            this.rootModule = rootModule;
        }
    }

    public void addModule(Module module) {
        module.setProject(this);
        this.addToModules(module);
    }

    public void setModules(List<Module> modules) {
        modules.forEach(module -> module.setProject(this));
        this.modules = new ArrayList<>(modules);
    }

    public void setRepository(Repository repository) {
        if (repository.getObjectContext() != null) {
            super.setRepository(repository);
        } else {
            this.repository = repository;
        }
    }

    public void setDependencies(Set<ProjectDependency> set) {
        set.forEach(project -> super.addToDependencies(project));
    }

    public void addDependenciesWithoutContext(List<Project> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public int compareTo(Project o) {
        return getRepository().compareTo(o.getRepository());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;
        return repository.equals(project.repository);
    }

    @JsonIgnore
    public String getVersion() {
        return getRootModule().getVersion();
    }

    @Override
    public int hashCode() {
        return repository.hashCode();
    }

    @Override
    public String toString() {
        return "Project{repository=" + repository + ", path=" + path + '}';
    }
}
