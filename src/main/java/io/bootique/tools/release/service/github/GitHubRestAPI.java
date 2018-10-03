package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.Repository;

import java.io.IOException;

public interface GitHubRestAPI {

    Milestone createMilestone(Repository repository, String title, String description) throws IOException;

    void renameMilestone(Repository repository, String title, String description, String newTitle);

    void closeMilestone(Repository repository, String title, String description);
}
