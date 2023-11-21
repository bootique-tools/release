package io.bootique.tools.release.service.release.stage.manager;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.service.release.ReleaseDescriptorFactory;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageManagerServiceTest {

    private final ReleaseDescriptorFactory releaseDescriptorFactory = new ReleaseDescriptorFactory();

    private StageManagerService createStageManager(ReleaseDescriptor releaseDescriptor) {
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(null);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        StageManagerImplService managerImplService = new StageManagerImplService();
        managerImplService.releaseDescriptorService = releaseDescriptorService;

        return managerImplService;
    }

    @Test
    void isStageSyncCurrentWithNotStartDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        assertFalse(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(0)));
        assertTrue(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void partialReleasePreparePerformFailure() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createPartialPrepareDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        assertFalse(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(0)));
        assertFalse(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void isStageSyncCurrentWithFailPerformDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        releaseDescriptor
                .getRepositoryDescriptorList()
                .get(1)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Fail);
        assertFalse(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void isStageSyncCurrentWithFaiRollbackPerformDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        releaseDescriptor
                .getRepositoryDescriptorList()
                .get(1)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Fail_Rollback);
        assertFalse(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void isStageSyncCurrentWithNotStartPerformDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        releaseDescriptor
                .getRepositoryDescriptorList()
                .get(1)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Not_Start);
        assertFalse(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void isStageSyncCurrentWithSkipPerformDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        releaseDescriptor
                .getRepositoryDescriptorList()
                .get(1)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Skip);
        assertTrue(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void isStageSyncCurrentWithSyncDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        StageManagerService managerService = createStageManager(releaseDescriptor);

        releaseDescriptor
                .getRepositoryDescriptorList()
                .get(0)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_SYNC, ReleaseStageStatus.Success);
        assertTrue(managerService.isStageSyncCurrent(releaseDescriptor.getRepositoryDescriptorList().get(1)));
    }

    @Test
    void releaseHaveFailStageWithFailStage() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(1);
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_SYNC, ReleaseStageStatus.Fail
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveFailStage());

        releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(2);
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PULL, ReleaseStageStatus.Fail
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveFailStage());

        releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(1);
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Fail_Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveFailStage());
    }

    @Test
    void releaseHaveFailStageWithoutFailStage() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createFinishDescriptor(1);
        assertFalse(createStageManager(releaseDescriptor).releaseHaveFailStage());

        releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(2);
        assertFalse(createStageManager(releaseDescriptor).releaseHaveFailStage());

        releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(4);
        assertFalse(createStageManager(releaseDescriptor).releaseHaveFailStage());
    }

    @Test
    void releaseHaveRollbackPerformStage() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(1);
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Fail_Rollback
        );
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        releaseDescriptor.getRepositoryDescriptorList().get(1).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor.getRepositoryDescriptorList().get(1).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Fail_Rollback
        );
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Fail_Rollback
        );
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
    }

    @Test
    void releaseHaveRollbackPrepareStage() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(1);
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PREPARE, ReleaseStageStatus.Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PREPARE, ReleaseStageStatus.Fail_Rollback
        );
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        releaseDescriptor.getRepositoryDescriptorList().get(1).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PREPARE, ReleaseStageStatus.Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor.getRepositoryDescriptorList().get(1).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PREPARE, ReleaseStageStatus.Fail_Rollback
        );
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());

        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PREPARE, ReleaseStageStatus.Rollback
        );
        assertTrue(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
        releaseDescriptor.getRepositoryDescriptorList().get(0).getStageStatusMap().replace(
                ReleaseStage.RELEASE_PREPARE, ReleaseStageStatus.Fail_Rollback
        );
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
    }

    @Test
    void releaseNotHaveRollbackPerformStageWithFinishDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createFinishDescriptor(1);
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
    }

    @Test
    void releaseNotHaveRollbackPerformStageWithNotStartDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(1);
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
    }

    @Test
    void releaseNotHaveRollbackPerformStageWithPerformDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(2);
        assertFalse(createStageManager(releaseDescriptor).releaseHaveRollbackStage());
    }
}