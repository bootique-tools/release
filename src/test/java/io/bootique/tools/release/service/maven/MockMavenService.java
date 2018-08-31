package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockMavenService implements MavenService {
    @Override
    public boolean isMavenProject(Repository repository) {
        return true;
    }

    @Override
    public Project createProject(Repository repository) {
        return new Project(repository, Paths.get(repository.getName()), new Module(repository.getName(), repository.getName(),"1.0.2"));
    }

    @Override
    public Module resolveRootModule(Path path) {
        return null;
    }

    @Override
    public List<Project> getProjects(Organization organization, Predicate<Project> predicate) {
        Repository repository = new Repository();
        repository.setName("test");
        return Stream.of(new Project(repository, Paths.get(repository.getName()), new Module(repository.getName(), repository.getName(),"1.0.2")))
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
