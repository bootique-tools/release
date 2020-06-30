package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.preferences.Preference;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public interface MavenService {

    Preference<String> ORGANIZATION_GROUP_ID = Preference.of("mvn.group.id", String.class);

    Preference<String> GROUP_ID_PATTERN = Preference.of("group.id", String.class);

    boolean isMavenProject(Repository repository);

    Project createProject(Repository repository);

    Module resolveModule(Path path);

    List<Project> getProjects(Organization organization, Predicate<Project> predicate);
}
