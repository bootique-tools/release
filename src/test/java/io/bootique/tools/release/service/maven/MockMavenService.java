package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.maven.persistent.Module;

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
    public Module resolveModule(Path path) {
        return null;
    }

    @Override
    public List<Project> getProjects(Organization organization, Predicate<Project> predicate) {
        Repository repository = organization.getObjectContext().newObject(Repository.class);
        repository.setName("test");
        return Stream.of(new Project(repository, Paths.get(repository.getName()), new Module(repository.getName(), repository.getName(),"1.0.2")))
                .filter(predicate)
                .collect(Collectors.toList());
    }

}
