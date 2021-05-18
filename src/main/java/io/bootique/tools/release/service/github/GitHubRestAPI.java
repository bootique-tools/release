package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;

public interface GitHubRestAPI {

    Milestone createMilestone(Repository repository, String title);

    void renameMilestone(Milestone milestone, String newTitle);

    void closeMilestone(Milestone milestone);
}
