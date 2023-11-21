package io.bootique.tools.release.service.release.executor.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReleaseTaskFactory implements TaskFactory {

    @Inject
    private StageUpdaterService stageUpdaterService;

    @Inject
    private ReleasePersistentService saverService;

    @Inject
    private Map<ReleaseStage, Function<Repository, String>> releaseTaskMap;

    @Override
    public Function<RepositoryDescriptor, String> createTask(Repository repository, List<ReleaseStage> executeStages) {
        return repositoryDescriptor -> {
            executeStages.forEach(stage -> executeRepoStage(repository, repositoryDescriptor, stage));
            return "";
        };
    }

    protected void executeRepoStage(Repository repository, RepositoryDescriptor repositoryDescriptor, ReleaseStage stage) {
        resetSubsequentStages(repositoryDescriptor, stage);
        stageUpdaterService.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.In_Progress);

        try {
            releaseTaskMap.get(stage).apply(repository);
            stageUpdaterService.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Success);
        } catch (Exception e) {
            stageUpdaterService.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Fail);
            throw e;
        } finally {
            saverService.saveRelease();
        }
    }

    private void resetSubsequentStages(RepositoryDescriptor repositoryDescriptor, ReleaseStage stage) {
        Map<ReleaseStage, ReleaseStageStatus> statusStageMap = repositoryDescriptor.getStageStatusMap();
        var stageArray = new ArrayList<>(statusStageMap.keySet());

        for (int i = stageArray.indexOf(stage) + 1; i < stageArray.size() - 1; i++) {
            statusStageMap.replace(stageArray.get(i), ReleaseStageStatus.Not_Start);
        }
        repositoryDescriptor.setStageStatusMap(statusStageMap);
    }
}
