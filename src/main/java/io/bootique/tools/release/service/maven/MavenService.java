package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.preferences.Preference;

import java.util.List;

public interface MavenService {

    Preference<String> ORGANIZATION_GROUP_ID = Preference.of("mvn.group.id", String.class);

    Preference<String> GROUP_ID_PATTERN = Preference.of("group.id", String.class);

    boolean isMavenProject(Repository repository);

    Project createOrUpdateProject(Repository repository);

    List<Project> sortProjects(List<Project> projects);

    void syncDependencies(Project project, List<Project> projects);
}
