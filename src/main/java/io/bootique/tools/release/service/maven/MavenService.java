package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.preferences.Preference;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public interface MavenService {

    Preference<String> ORGANIZATION_GROUP_ID = Preference.of("mvn.group.id", String.class);

    boolean isMavenProject(Repository repository);

    Project createProject(Repository repository);

    Module resolveRootModule(Path path);

    List<Project> getProjects(Organization organization, Predicate<Project> predicate);

    List<Project> getProjectsWithoutDependencies(Organization organization, Predicate<Project> predicate);
}
