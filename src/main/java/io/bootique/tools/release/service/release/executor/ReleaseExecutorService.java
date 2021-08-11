package io.bootique.tools.release.service.release.executor;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

public interface ReleaseExecutorService {

    void executeRelease();

    void startSyncStage();

    void skipRepository(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage);

    void restartExecute(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage);
}
