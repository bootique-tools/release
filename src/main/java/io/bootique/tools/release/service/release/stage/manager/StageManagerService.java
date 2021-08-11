package io.bootique.tools.release.service.release.stage.manager;

import io.bootique.tools.release.model.release.RepositoryDescriptor;

public interface StageManagerService {

    void dropInProgressStage(RepositoryDescriptor repositoryDescriptor);

    boolean isStageSyncCurrent(RepositoryDescriptor repositoryDescriptor);

    boolean releaseHaveFailStage();

    boolean releaseHaveRollbackStage();
}
