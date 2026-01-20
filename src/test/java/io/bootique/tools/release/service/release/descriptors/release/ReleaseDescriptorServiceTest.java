package io.bootique.tools.release.service.release.descriptors.release;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.ReleaseDescriptorFactory;
import io.bootique.tools.release.service.release.persistent.MockReleasePersistentService;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReleaseDescriptorServiceTest {

    private final ReleaseDescriptorFactory releaseDescriptorFactory = new ReleaseDescriptorFactory();

    @Test
    void getNullNotSavedReleaseDescriptor() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(null);

        assertNull(releaseDescriptorService.getReleaseDescriptor());
    }

    @Test
    void getNullSavedReleaseDescriptor() {
        Provider<ReleasePersistentService> provider = () -> new MockReleasePersistentService(true);
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(null);

        assertNotNull(releaseDescriptorService.getReleaseDescriptor());
    }

    @Test
    void getLoadReleaseDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(2);

        Provider<ReleasePersistentService> provider = () -> mock(ReleasePersistentService.class);
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        assertEquals(releaseDescriptorService.getReleaseDescriptor(),releaseDescriptor);
    }

    @Test
    void getUnfinishedRepositoryDescriptorListWithNotStartDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotStartDescriptor(2);
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        assertEquals(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PULL),
                releaseDescriptor.getRepositoryDescriptorList());
    }

    @Test
    void getUnfinishedRepositoryDescriptorListWithFinishDescriptor() {
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createFinishDescriptor(2));

        assertEquals(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PULL), Collections.emptyList());
    }

    @Test
    void getUnfinishedRepositoryDescriptorList() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(4);
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        releaseDescriptor.getRepositoryDescriptorList()
                .get(0)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Not_Start);

        assertEquals(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PULL),
                releaseDescriptor.getRepositoryDescriptorList().subList(0, 1));
    }

    @Test
    void getRestartUnfinishedRepositoryDescriptorList() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(4);
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        releaseDescriptor.getRepositoryDescriptorList()
                .get(1)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Reload);

        List<RepositoryDescriptor> unfinishedRepositoryDescriptorList = releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PULL);
        assertEquals(releaseDescriptor.getRepositoryDescriptorList().subList(1,2), unfinishedRepositoryDescriptorList);
    }

    @Test
    void getSkipUnfinishedRepositoryDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(4);
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        releaseDescriptor.getRepositoryDescriptorList()
                .get(0)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Skip);

        List<RepositoryDescriptor> unfinishedRepositoryDescriptorList = releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PULL);
        assertEquals(releaseDescriptor.getRepositoryDescriptorList(), unfinishedRepositoryDescriptorList);
    }

    @Test
    void getSkipReleasePerformUnfinishedRepositoryDescriptor() {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createPartialPrepareDescriptor(2);
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        releaseDescriptor.getRepositoryDescriptorList()
                .get(0)
                .getStageStatusMap()
                .replace(ReleaseStage.RELEASE_PERFORM, ReleaseStageStatus.Skip);

        List<RepositoryDescriptor> unfinishedRepositoryDescriptorList
                = releaseDescriptorService.getUnfinishedRepositoryDescriptorList(ReleaseStage.RELEASE_PERFORM);
        assertEquals(releaseDescriptor.getRepositoryDescriptorList().subList(1, 2),
                unfinishedRepositoryDescriptorList);
    }
}