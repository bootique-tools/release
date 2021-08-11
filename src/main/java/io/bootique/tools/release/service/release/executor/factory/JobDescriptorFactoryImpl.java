package io.bootique.tools.release.service.release.executor.factory;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.job.ErrorPolicy;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorService;
import io.bootique.tools.release.service.release.executor.tasks.ReleaseTaskFactory;
import io.bootique.tools.release.service.release.executor.tasks.RollbackTaskFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JobDescriptorFactoryImpl implements JobDescriptorFactory {

    @Inject
    private RepositoryDescriptorService repositoryDescriptorService;

    @Inject
    private ReleaseTaskFactory releaseTaskFactory;

    @Inject
    private RollbackTaskFactory rollbackTaskFactory;

    @Override
    public BatchJobDescriptor<RepositoryDescriptor, String> createReleaseJobDescriptor(List<RepositoryDescriptor> data) {
        List<ReleaseStage> stages = getExecuteStages(data);

        return BatchJobDescriptor.<RepositoryDescriptor, String>builder()
                .data(data)
                .processor(repositoryDescriptor -> {
                    Repository repository = repositoryDescriptorService.loadRepository(repositoryDescriptor);
                    return releaseTaskFactory.createTask(repository, stages).apply(repositoryDescriptor);
                })
                .errorPolicy(ErrorPolicy.ABORT_ALL_ON_ERROR)
                .build();
    }

    private List<ReleaseStage> getExecuteStages(List<RepositoryDescriptor> data) {
        ReleaseStage stage = getCurrentStage(data.get(0));
        List<ReleaseStage> executeStages = new ArrayList<>();

        executeStages.add(stage);
        if (stage == ReleaseStage.RELEASE_PREPARE) {
            executeStages.add(ReleaseStage.RELEASE_PERFORM);
        }
        return executeStages;
    }

    private ReleaseStage getCurrentStage(RepositoryDescriptor repositoryDescriptor) {
        return repositoryDescriptor.getStageStatusMap().entrySet().stream().filter(
                stageStatusEntry -> stageStatusEntry.getValue() == ReleaseStageStatus.Not_Start || stageStatusEntry.getValue() == ReleaseStageStatus.Reload
        ).findFirst().get().getKey();
    }

    @Override
    public BatchJobDescriptor<RepositoryDescriptor, String> createRollbackJobDescriptor(RepositoryDescriptor data,
                                                                                        ReleaseStage stage) {
        return BatchJobDescriptor.<RepositoryDescriptor, String>builder()
                .data(Collections.singletonList(data))
                .processor(repositoryDescriptor -> {
                    Repository repository = repositoryDescriptorService.loadRepository(repositoryDescriptor);
                    return rollbackTaskFactory
                            .createTask(repository, Collections.singletonList(stage))
                            .apply(repositoryDescriptor);
                })
                .errorPolicy(ErrorPolicy.ABORT_ALL_ON_ERROR)
                .build();
    }

    public BatchJobDescriptor<RepositoryDescriptor, String> createRollbackJobDescriptor(List<RepositoryDescriptor> data) {

        return BatchJobDescriptor.<RepositoryDescriptor, String>builder()
                .data(data)
                .processor(repositoryDescriptor -> {
                    Repository repository = repositoryDescriptorService.loadRepository(repositoryDescriptor);
                    return rollbackTaskFactory.createTask(repository, Arrays.asList(ReleaseStage.values())
                            .subList(1, ReleaseStage.values().length - 1)).apply(repositoryDescriptor);
                })
                .errorPolicy(ErrorPolicy.SKIP_ON_ERROR)
                .build();
    }
}