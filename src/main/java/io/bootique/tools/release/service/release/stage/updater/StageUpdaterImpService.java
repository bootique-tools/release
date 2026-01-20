package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.Map;

public class StageUpdaterImpService implements StageUpdaterService, StageListener {

    private static final OutProcessor NULL_PROCESSOR = (repositoryDescriptor, out) -> {};

    Map<ReleaseStage, OutProcessor> outProcessors;

    private Runnable stageListener;

    public StageUpdaterImpService() {
        outProcessors = Map.of(ReleaseStage.RELEASE_PERFORM, new CentralDescriptorIdExtractor());
    }

    @Override
    public void addListener(Runnable stageListener) {
        this.stageListener = stageListener;
    }

    @Override
    public void removeListener() {
        this.stageListener = null;
    }

    @Override
    public void updateStage(RepositoryDescriptor repositoryDescriptor,
                            ReleaseStage releaseStage,
                            ReleaseStageStatus status,
                            String out) {
        repositoryDescriptor.getStageStatusMap().replace(releaseStage, status);
        outProcessors.getOrDefault(releaseStage, NULL_PROCESSOR).accept(repositoryDescriptor, out);
        if(stageListener != null) {
            stageListener.run();
        }
    }
}