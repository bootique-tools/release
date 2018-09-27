package io.bootique.tools.release.model.maven;

public class Dependency implements Comparable<Dependency> {

    private final String type;
    private final Module module;

    public Dependency(String groupId, String artifactId, String version, String type) {
        this.module = new Module(groupId, artifactId, version);
        this.type = type;
    }

    public Module getModule() {
        return module;
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
