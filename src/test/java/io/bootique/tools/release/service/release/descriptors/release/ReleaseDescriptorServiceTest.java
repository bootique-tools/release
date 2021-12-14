package io.bootique.tools.release.service.release.descriptors.release;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.service.release.ReleaseDescriptorFactory;
import io.bootique.tools.release.service.release.persistent.MockReleasePersistentService;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReleaseDescriptorServiceTest {

    private final ReleaseDescriptorFactory releaseDescriptorFactory = new ReleaseDescriptorFactory();

    @Test
    void getNullNotSavedReleaseDescriptor() {
        Provider<ReleasePersistentService> provider = MockReleasePersistentService::new;
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(null);

        assertNull(((ReleaseDescriptorService) releaseDescriptorService).getReleaseDescriptor());
    }

    @Test
    void getNullSavedReleaseDescriptor() {
        Provider<ReleasePersistentService> provider = () -> new MockReleasePersistentService(true);
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(provider);
        releaseDescriptorService.setReleaseDescriptor(null);

        assertNotNull(((ReleaseDescriptorService) releaseDescriptorService).getReleaseDescriptor());
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

        assertEquals(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(),
                releaseDescriptor.getRepositoryDescriptorList());
    }

    @Test
    void getUnfinishedRepositoryDescriptorListWithFinishDescriptor() {
        ReleaseDescriptorServiceImpl releaseDescriptorService =
                new ReleaseDescriptorServiceImpl(() -> mock(ReleasePersistentService.class));
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptorFactory.createFinishDescriptor(2));

        assertEquals(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(), Collections.emptyList());
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

        assertEquals(releaseDescriptorService.getUnfinishedRepositoryDescriptorList(),
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

        assertEquals(releaseDescriptor.getRepositoryDescriptorList().subList(1,2),
                releaseDescriptorService.getUnfinishedRepositoryDescriptorList());
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

        assertEquals(releaseDescriptor.getRepositoryDescriptorList(),
                releaseDescriptorService.getUnfinishedRepositoryDescriptorList());
    }
}