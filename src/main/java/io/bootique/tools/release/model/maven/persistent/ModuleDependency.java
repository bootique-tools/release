package io.bootique.tools.release.model.maven.persistent;

import io.bootique.tools.release.model.maven.persistent.auto._ModuleDependency;
import org.apache.cayenne.ObjectContext;

public class ModuleDependency extends _ModuleDependency implements Comparable<ModuleDependency> {

    private static final long serialVersionUID = 1L;

    public ModuleDependency() {
    }

    public ModuleDependency(String groupId, String artifactId, String version, String type, ObjectContext context) {
        this.module = new Module(groupId, artifactId, version);
        this.type = type;
        if (context != null) {
            context.registerNewObject(this.module);
        }
    }

    public ModuleDependency(Module module, String type, ObjectContext context) {
        this.module = module;
        this.type = type;
        if (module.getObjectContext() == null) {
            context.registerNewObject(this.module);
        }
    }

    @Override
    public int compareTo(ModuleDependency o) {
        return getModule().compareTo(o.getModule());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleDependency that = (ModuleDependency) o;
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
