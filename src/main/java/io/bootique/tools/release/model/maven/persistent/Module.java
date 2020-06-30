package io.bootique.tools.release.model.maven.persistent;

import io.bootique.tools.release.model.maven.persistent.auto._Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Module extends _Module implements Comparable<Module> {

    private static final long serialVersionUID = 1L;

    public Module() {
    }

    public Module(String id) {
        super.setGithubId(id);
    }

    public Module(String groupStr, String githubId, String version) {
        this.groupStr = Objects.requireNonNull(groupStr);
        this.githubId = Objects.requireNonNull(githubId);
        this.version = Objects.requireNonNull(version);
        this.dependencies = new ArrayList<>();
    }

    public void addDependenciesWithoutContext(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        if (!groupStr.equals(module.groupStr)) return false;
        return githubId.equals(module.githubId);
    }

    @Override
    public int hashCode() {
        int result = groupStr.hashCode();
        result = 31 * result + githubId.hashCode();
        return result;
    }

    @Override
    public int compareTo(Module o) {
        int res = groupStr.compareTo(o.groupStr);
        if (res != 0) {
            return res;
        }
        return githubId.compareTo(o.githubId);
    }

    @Override
    public String toString() {
        return "Module{" +
                "groupStr='" + groupStr + '\'' +
                ", id='" + githubId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
