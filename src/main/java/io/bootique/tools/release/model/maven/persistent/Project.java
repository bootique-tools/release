package io.bootique.tools.release.model.maven.persistent;

import io.bootique.tools.release.model.maven.persistent.auto._Project;

import java.nio.file.Path;

public class Project extends _Project implements Comparable<Project> {

    private static final long serialVersionUID = 1L;

    private Path path;

    public Project() {
        super();
    }

    public Path getPath() {
        if (path == null) {
            path = Path.of(getPathStr());
        }
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        this.pathStr = path.toString();
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
        return getRepository().equals(project.getRepository());
    }

    @Override
    public int hashCode() {
        return getRepository().hashCode();
    }

    @Override
    public String toString() {
        return "Project{repository=" + repository + ", path=" + path + '}';
    }
}
