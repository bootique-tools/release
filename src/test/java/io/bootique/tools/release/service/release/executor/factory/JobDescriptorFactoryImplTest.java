package io.bootique.tools.release.service.release.executor.factory;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.service.release.ReleaseDescriptorFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobDescriptorFactoryImplTest {

    private final ReleaseDescriptorFactory releaseDescriptorFactory = new ReleaseDescriptorFactory();

    @Test
    void getExecuteStagesWithNotStartDescriptor() {
        JobDescriptorFactoryImpl jobDescriptorFactory = new JobDescriptorFactoryImpl();

        assertEquals(jobDescriptorFactory.getExecuteStages(
                        releaseDescriptorFactory.createNotStartDescriptor(3).getRepositoryDescriptorList()),
                List.of(ReleaseStage.RELEASE_PULL)
        );
    }

    @Test
    void getExecuteStagesWithPrepareDescriptor() {
        JobDescriptorFactoryImpl jobDescriptorFactory = new JobDescriptorFactoryImpl();
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(1);
        releaseDescriptor.getRepositoryDescriptorList().get(0).
                getStageStatusMap().replace(ReleaseStage.RELEASE_PULL, ReleaseStageStatus.Success);
        releaseDescriptor.getRepositoryDescriptorList().get(0).
                getStageStatusMap().replace(ReleaseStage.RELEASE_VALIDATION, ReleaseStageStatus.Success);

        assertEquals(jobDescriptorFactory.getExecuteStages(releaseDescriptor.getRepositoryDescriptorList()),
                List.of(ReleaseStage.RELEASE_PREPARE, ReleaseStage.RELEASE_PERFORM)
        );
    }

    @Test
    void getExecuteStagesWithPerformDescriptor() {
        JobDescriptorFactoryImpl jobDescriptorFactory = new JobDescriptorFactoryImpl();

        assertEquals(jobDescriptorFactory.getExecuteStages(
                        releaseDescriptorFactory.createNotSyncDescriptor(2).getRepositoryDescriptorList()),
                List.of(ReleaseStage.RELEASE_SYNC)
        );
    }

    @Test
    void getExecuteStagesWithFinishDescriptor() {
        JobDescriptorFactoryImpl jobDescriptorFactory = new JobDescriptorFactoryImpl();

        assertEquals(jobDescriptorFactory.getExecuteStages(
                        releaseDescriptorFactory.createFinishDescriptor(2).getRepositoryDescriptorList()),
                Collections.emptyList()
        );
    }
}