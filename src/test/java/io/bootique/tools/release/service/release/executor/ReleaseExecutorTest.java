package io.bootique.tools.release.service.release.executor;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.service.release.ReleaseDescriptorFactory;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import io.bootique.tools.release.service.release.persistent.MockReleasePersistentService;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.stage.manager.MockReleaseManagerFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseExecutorTest {

    private final ReleaseDescriptorFactory releaseDescriptorFactory = new ReleaseDescriptorFactory();
    private final MockReleaseManagerFactory releaseManagerFactory = new MockReleaseManagerFactory();

    @Test
    void releaseNotFinishWithoutFinishDescriptor() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createNotSyncDescriptor(1));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        assertTrue(releaseExecutor.releaseInProgress());
    }

    @Test
    void releaseNotFinishWithFinishDescriptor() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createFinishDescriptor(3));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        assertFalse(releaseExecutor.releaseInProgress());
    }

    @Test
    void releaseCanRunningWithFinishDescriptor() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createFinishDescriptor(3));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        releaseExecutor.stageManager = releaseManagerFactory.createStageManager(releaseDescriptorService.getReleaseDescriptor());
        assertFalse(releaseExecutor.canExecuteRelease());
    }

    @Test
    void releaseCanRunningWithPerformStage() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createNotSyncDescriptor(3));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        releaseExecutor.stageManager = releaseManagerFactory.createStageManager(releaseDescriptorService.getReleaseDescriptor());

        assertFalse(releaseExecutor.canExecuteRelease());
    }

    @Test
    void releaseCanRunningWithStartSyncStage() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createNotSyncDescriptor(3));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        releaseExecutor.stageManager = releaseManagerFactory.createStageManager(releaseDescriptorService.getReleaseDescriptor());
        releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().get(0)
                .getStageStatusMap().replace(ReleaseStage.RELEASE_SYNC,ReleaseStageStatus.Skip);
        assertTrue(releaseExecutor.canExecuteRelease());
    }

    @Test
    void releaseCanRunningWithStartFailStage() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createNotSyncDescriptor(1));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        releaseExecutor.stageManager = releaseManagerFactory.createStageManager(releaseDescriptorService.getReleaseDescriptor());
        releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().get(0)
                .getStageStatusMap().replace(ReleaseStage.RELEASE_VALIDATION,ReleaseStageStatus.Fail);
        assertFalse(releaseExecutor.canExecuteRelease());

        releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().get(0)
                .getStageStatusMap().replace(ReleaseStage.RELEASE_VALIDATION,ReleaseStageStatus.Success);
        releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().get(0)
                .getStageStatusMap().replace(ReleaseStage.RELEASE_PERFORM,ReleaseStageStatus.Fail_Rollback);
        assertFalse(releaseExecutor.canExecuteRelease());
    }

    @Test
    void releaseCanRunningWithStartRollbackStage() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createNotSyncDescriptor(1));

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        releaseExecutor.stageManager = releaseManagerFactory.createStageManager(releaseDescriptorService.getReleaseDescriptor());
        releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().get(0)
                .getStageStatusMap().replace(ReleaseStage.RELEASE_PREPARE,ReleaseStageStatus.Rollback);
        assertFalse(releaseExecutor.canExecuteRelease());
    }


    @Test
    void isReleaseNotRunning() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createFinishDescriptor(1);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        ReleaseExecutor releaseExecutor = new ReleaseExecutor();
        releaseExecutor.releaseDescriptorService = releaseDescriptorService;
        assertTrue(releaseExecutor.releaseNotRunning());

        releaseDescriptor.getRepositoryDescriptorList().get(0)
                .getStageStatusMap().replace(ReleaseStage.RELEASE_SYNC, ReleaseStageStatus.In_Progress);
        assertFalse(releaseExecutor.releaseNotRunning());
    }
}