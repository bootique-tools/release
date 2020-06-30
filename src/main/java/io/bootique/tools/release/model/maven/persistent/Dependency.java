package io.bootique.tools.release.model.maven.persistent;

import io.bootique.tools.release.model.maven.persistent.auto._Dependency;
import org.apache.cayenne.ObjectContext;

public class Dependency extends _Dependency implements Comparable<Dependency> {

    private static final long serialVersionUID = 1L;

    public Dependency() {
    }

    public Dependency(String groupId, String artifactId, String version, String type, ObjectContext context) {
        this.module = new Module(groupId, artifactId, version);
        this.type = type;
    }

    public Dependency(Module module, String type, ObjectContext context) {
        this.module = module;
        this.type = type;
    }

    @Override
    public int compareTo(Dependency o) {
        return getModule().compareTo(o.getModule());
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
