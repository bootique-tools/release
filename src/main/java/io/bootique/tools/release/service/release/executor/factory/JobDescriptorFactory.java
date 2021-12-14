package io.bootique.tools.release.service.release.executor.factory;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.job.ErrorPolicy;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.List;

public interface JobDescriptorFactory {

    BatchJobDescriptor<RepositoryDescriptor, String> createReleaseJobDescriptor(List<RepositoryDescriptor> data);

    BatchJobDescriptor<RepositoryDescriptor, String> createRollbackJobDescriptor(RepositoryDescriptor data,
                                                                                 ReleaseStage stage);
}
