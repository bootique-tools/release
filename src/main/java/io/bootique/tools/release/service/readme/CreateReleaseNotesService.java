package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.persistent.Repository;

import java.util.List;

public interface CreateReleaseNotesService {

    StringBuilder createReleaseNotes(List<Repository> repositories, String milestoneTitle);

}
