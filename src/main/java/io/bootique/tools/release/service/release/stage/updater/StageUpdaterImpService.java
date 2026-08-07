package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class StageUpdaterImpService implements StageUpdaterService, StageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageUpdaterImpService.class);

    private static final OutProcessor NULL_PROCESSOR = (repositoryDescriptor, out) -> {};

    Map<ReleaseStage, OutProcessor> outProcessors;

    // stages are updated from release worker threads while listeners come and go with client connections
    private final List<Runnable> stageListeners = new CopyOnWriteArrayList<>();

    public StageUpdaterImpService() {
        outProcessors = Map.of(ReleaseStage.RELEASE_PERFORM, new CentralDescriptorIdExtractor());
    }

    @Override
    public void addListener(Runnable stageListener) {
        stageListeners.add(stageListener);
    }

    @Override
    public void removeListener(Runnable stageListener) {
        stageListeners.remove(stageListener);
    }

    @Override
    public void updateStage(RepositoryDescriptor repositoryDescriptor,
                            ReleaseStage releaseStage,
                            ReleaseStageStatus status,
                            String out) {
        repositoryDescriptor.getStageStatusMap().replace(releaseStage, status);
        outProcessors.getOrDefault(releaseStage, NULL_PROCESSOR).accept(repositoryDescriptor, out);

        // a listener is a client notification, never a reason to fail the stage that just succeeded,
        // and one broken listener must not hide the transition from the others
        for (Runnable listener : stageListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                LOGGER.warn("Stage listener failed", e);
            }
        }
    }
}