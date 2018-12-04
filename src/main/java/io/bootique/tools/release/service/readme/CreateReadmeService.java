package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Project;

import java.util.List;

public interface CreateReadmeService {

    StringBuilder createReadme(List<Repository> repositories, String milestoneTitle);

}
