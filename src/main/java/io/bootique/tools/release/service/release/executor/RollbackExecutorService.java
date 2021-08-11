package io.bootique.tools.release.service.release.executor;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

public interface RollbackExecutorService {

    void rollbackRepository(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage);

    void rollbackRelease();

    void skip(RepositoryDescriptor repositoryDescriptor, ReleaseStage valueOf);
}
