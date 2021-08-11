package io.bootique.tools.release.service.logger;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

public interface ExecutionLogger {

    String getLogs(RepositoryDescriptor repositoryDescriptor, ReleaseStage releaseStage);

    void writeLogs(String repositoryName, String stage, String text);
}
