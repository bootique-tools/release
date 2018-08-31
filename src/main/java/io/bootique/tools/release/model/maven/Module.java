package io.bootique.tools.release.model.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Module implements Comparable<Module> {

    private String group;
    private String id;
    private String version;
    @JsonIgnore
    private Set<Dependency> dependencies;
    @JsonIgnore
    private Project project;

    public Module(){}

    public Module(String group, String id, String version) {
        this.group = Objects.requireNonNull(group);
        this.id = Objects.requireNonNull(id);
        this.version = Objects.requireNonNull(version);
        this.dependencies = new HashSet<>();
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }

    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        if (!group.equals(module.group)) return false;
        return id.equals(module.id);
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public int compareTo(Module o) {
        int res = group.compareTo(o.group);
        if(res != 0) {
            return res;
        }
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "Module{" +
                "group='" + group + '\'' +
                ", id='" + id + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
