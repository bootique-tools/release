package io.bootique.tools.release.service.release.executor.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterService;
import io.bootique.tools.release.service.tasks.ReleaseTask;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RollbackTaskFactory implements TaskFactory {

    @Inject
    private StageUpdaterService stageUpdaterService;

    @Inject
    Map<RollbackStage, ReleaseTask> rollbackMap;

    @Inject
    private ReleasePersistentService saverService;

    @Override
    public Function<RepositoryDescriptor, String> createTask(Repository repository, List<ReleaseStage> executeStages) {

        List<Function<Repository, String>> functionList = new ArrayList<>();
        if (executeStages.contains(ReleaseStage.RELEASE_PREPARE)) {
            functionList.add(rollbackMap.get(RollbackStage.ROLLBACK_MVN));
        }
        if (executeStages.contains(ReleaseStage.RELEASE_PERFORM)) {
            functionList.add(rollbackMap.get(RollbackStage.ROLLBACK_SONATYPE));
        }

        return repositoryDescriptor -> {

            functionList.forEach(function -> {

                 executeStages.forEach(stage ->
                        stageUpdaterService.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.In_Progress));
                try {
                    function.apply(repository);

                    executeStages.forEach(stage ->
                            stageUpdaterService.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Rollback));
                } catch (Exception e) {
                    executeStages.forEach(stage ->
                            stageUpdaterService.updateStage(repositoryDescriptor, stage, ReleaseStageStatus.Fail_Rollback));
                    throw e;
                } finally {
                    saverService.saveRelease();
                }
            });
            return "";
        };
    }
}
