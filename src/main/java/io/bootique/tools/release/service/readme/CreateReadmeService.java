package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.persistent.Repository;

import java.util.List;

public interface CreateReadmeService {

    StringBuilder createReadme(List<Repository> repositories, String milestoneTitle);

}
