package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

public interface StageUpdaterService {
    void updateStage(RepositoryDescriptor repositoryDescriptor, ReleaseStage releaseStage, ReleaseStageStatus status);
}
