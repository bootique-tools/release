package io.bootique.tools.release.model.maven;

import java.util.Objects;

public class Dependency implements Comparable<Dependency> {

    // io.bootique:bootique:jar:0.26-SNAPSHOT:compile
    private final String name;
    private final String artifact;
    private final String type;
    private final Module module;

    public Dependency(String name) {
        this.name = Objects.requireNonNull(name);
        String[] parts = name.split(":");
        if(parts.length < 5) {
            throw new RuntimeException("Unable to parse dependency string " + name);
        }
        this.module = new Module(parts[0], parts[1], parts[3]);
        this.artifact = parts[2];
        this.type = parts[4];
    }

    public String getName() {
        return name;
    }

    public Module getModule() {
        return module;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(Dependency o) {
        return module.compareTo(o.module);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return module.equals(that.module);
    }

    @Override
    public int hashCode() {
        return module.hashCode();
    }

    @Override
    public String toString() {
        return "Dependency{" + module + '}';
    }
}
