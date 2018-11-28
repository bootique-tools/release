package io.bootique.tools.release.model.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bootique.tools.release.model.github.Repository;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Project implements Comparable<Project> {

    private Repository repository;

    @JsonIgnore
    private Path path;

    private Module rootModule;

    private boolean disable;

    @JsonIgnore
    private Set<Module> modules;

    private Set<Project> dependencies;

    private String branchName;

    public Project(){}

    public Project(Repository repository, Path path, Module rootModule) {
        this.repository = Objects.requireNonNull(repository);
        this.path = path;
        this.rootModule = rootModule;
        this.modules = new HashSet<>();
        this.dependencies = new HashSet<>();
        rootModule.setProject(this);
    }

    public Repository getRepository() {
        return repository;
    }

    public Path getPath() {
        return path;
    }

    public void addModule(Module module) {
        module.setProject(this);
        modules.add(module);
    }

    public void setModules(Set<Module> modules) {
        modules.forEach(module -> module.setProject(this));
        this.modules = modules;
    }

    public Set<Module> getModules() {
        return modules;
    }

    public Set<Project> getDependencies() {
        return dependencies;
    }

    public Module getRootModule() {
        return rootModule;
    }

    public boolean isDisable() {
        return disable;
    }

    @Override
    public int compareTo(Project o) {
        return repository.compareTo(o.repository);
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
        return rootModule.getVersion();
    }

    @Override
    public int hashCode() {
        return repository.hashCode();
    }

    @Override
    public String toString() {
        return "Project{repository=" + repository + ", path=" + path + '}';
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
