package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.Optional;

public class StageUpdaterImpService implements StageUpdaterService, StageListener {

    private Optional<Runnable> stageListener = Optional.empty();

    @Override
    public void addListener(Runnable stageListener) {
        this.stageListener = Optional.ofNullable(stageListener);
    }

    @Override
    public void removeListener() {
        this.stageListener = Optional.empty();
    }

    @Override
    public void updateStage(RepositoryDescriptor repositoryDescriptor, ReleaseStage releaseStage, ReleaseStageStatus status) {
        repositoryDescriptor.getStageStatusMap().replace(releaseStage, status);
        stageListener.ifPresent(Runnable::run);
    }
}