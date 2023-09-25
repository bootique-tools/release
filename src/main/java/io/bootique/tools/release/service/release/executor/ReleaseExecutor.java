package io.bootique.tools.release.service.release.executor;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.job.BatchJobStatus;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.logger.ExecutionLogger;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.executor.factory.JobDescriptorFactoryImpl;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.stage.manager.StageManagerService;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterService;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Named;

@Service
public class ReleaseExecutor implements ReleaseExecutorService {

    @Inject
    protected BatchJobService jobService;

    @Inject
    protected ReleaseDescriptorService releaseDescriptorService;

    @Inject
    protected JobDescriptorFactoryImpl jobDescriptorCreator;

    @Inject
    protected StageManagerService stageManager;

    @Inject
    protected ReleasePersistentService saverService;

    @Inject
    @Named("release")
    protected ExecutionLogger executionLogger;

    @Inject
    protected StageUpdaterService stageUpdater;

    @Override
    public void executeRelease() {
        if (canExecuteRelease() && releaseNotRunning()) {
            BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor = jobDescriptorCreator
                    .createReleaseJobDescriptor(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PULL));
            executeReleaseStage(jobDescriptor);
        }
    }

    protected boolean releaseInProgress() {
        return releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList()
                .stream()
                .map(repo -> repo.getStageStatusMap().get(ReleaseStage.RELEASE_SYNC))
                .anyMatch(status -> status != ReleaseStageStatus.Success && status != ReleaseStageStatus.Skip);
    }

    protected boolean canExecuteRelease() {
        if (!stageManager.releaseHaveFailStage() && !stageManager.releaseHaveRollbackStage()) {
            boolean performStageFinished = releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList()
                    .stream()
                    .map(repo -> repo.getStageStatusMap().get(ReleaseStage.RELEASE_PERFORM))
                    .allMatch(performStatus -> performStatus == ReleaseStageStatus.Success || performStatus == ReleaseStageStatus.Skip);
            if (performStageFinished) {
                return releaseInProgress() && syncStageStarted();
            }
            return true;
        }
        return false;
    }

    private boolean syncStageStarted() {
        return releaseDescriptorService
                .getReleaseDescriptor()
                .getRepositoryDescriptorList()
                .stream()
                .anyMatch(repositoryDescriptor ->
                        repositoryDescriptor.getStageStatusMap().get(ReleaseStage.RELEASE_SYNC) != ReleaseStageStatus.Not_Start
                );
    }

    protected boolean releaseNotRunning() {
        return releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList()
                .stream()
                .noneMatch(descriptor -> descriptor.getStageStatusMap().containsValue(ReleaseStageStatus.In_Progress));

    }

    @Override
    public void startSyncStage() {
        BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor = jobDescriptorCreator
                .createReleaseJobDescriptor(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_SYNC));
        executeReleaseStage(jobDescriptor);
    }

    @Override
    public void skipRepository(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage) {
        executionLogger.writeLogs(repositoryDescriptor.getRepositoryName(), stage.name(), ReleaseStageStatus.Skip.getMessage());

        stageUpdater.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Skip);
        saverService.saveRelease();

        BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor = jobDescriptorCreator
                .createReleaseJobDescriptor(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(stage));
        executeReleaseStage(jobDescriptor);

    }

    @Override
    public void restartExecute(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage) {
        executionLogger.writeLogs(repositoryDescriptor.getRepositoryName(), stage.name(), ReleaseStageStatus.Reload.getMessage());

        stageUpdater.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Reload);

        BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor = jobDescriptorCreator
                .createReleaseJobDescriptor(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(stage));
        executeReleaseStage(jobDescriptor);
    }

    private void executeReleaseStage(BatchJobDescriptor<RepositoryDescriptor, String> jobDescriptor) {
        var job = jobService.submit(jobDescriptor);
        job.addListener(() -> {
            if (job.isDone()) {
                var jobResult = job.getResults().get(job.getDone() - 1);
                if (!(jobResult.status() == BatchJobStatus.FAILURE || stageManager.isStageSyncCurrent(jobResult.data()))) {
                    executeRelease();
                }
            }
        });
    }
}






