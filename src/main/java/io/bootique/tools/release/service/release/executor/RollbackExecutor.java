package io.bootique.tools.release.service.release.executor;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.logger.ExecutionLogger;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.executor.factory.JobDescriptorFactoryImpl;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterService;

import javax.inject.Inject;
import javax.inject.Named;

public class RollbackExecutor implements RollbackExecutorService {

    @Inject
    private BatchJobService jobService;

    @Inject
    private JobDescriptorFactoryImpl jobDescriptorCreator;

    @Inject
    private ReleaseDescriptorService releaseDescriptorService;

    @Inject
    private StageUpdaterService stageUpdater;

    @Inject
    private ReleasePersistentService saverService;

    @Inject
    @Named("rollback")
    public ExecutionLogger executionLogger;

    @Override
    public void rollbackRepository(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage) {
        BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor = jobDescriptorCreator
                .createRollbackJobDescriptor(repositoryDescriptor, stage);
        jobService.submit(jobDescriptor);
    }

    @Override
    public void rollbackRelease() {
        BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor = jobDescriptorCreator
                .createRollbackJobDescriptor(releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList());
        jobService.submit(jobDescriptor);
    }

    @Override
    public void skip(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage) {
        executionLogger.writeLogs(repositoryDescriptor.getRepositoryName(),stage.name(), ReleaseStageStatus.Rollback + " skipped");
        stageUpdater.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Rollback);
        saverService.saveRelease();
    }
}
