package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StageUpdaterImpServiceTest {

    private StageUpdaterImpService service;
    private RepositoryDescriptor descriptor;

    @BeforeEach
    void setUp() {
        service = new StageUpdaterImpService();
        descriptor = new RepositoryDescriptor("bootique", new EnumMap<>(Map.of(
                ReleaseStage.RELEASE_PULL, ReleaseStageStatus.Not_Start)));
    }

    private void update(ReleaseStageStatus status) {
        service.updateStage(descriptor, ReleaseStage.RELEASE_PULL, status, "");
    }

    @Test
    public void updatesStageStatusWithoutListeners() {
        update(ReleaseStageStatus.Success);

        assertEquals(ReleaseStageStatus.Success, descriptor.getStageStatusMap().get(ReleaseStage.RELEASE_PULL));
    }

    @Test
    public void notifiesEveryRegisteredListener() {
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        service.addListener(first::incrementAndGet);
        service.addListener(second::incrementAndGet);

        update(ReleaseStageStatus.In_Progress);

        assertEquals(1, first.get());
        assertEquals(1, second.get());
    }

    @Test
    public void aFailingListenerNeitherStopsTheOthersNorFailsTheStage() {
        AtomicInteger later = new AtomicInteger();
        service.addListener(() -> {
            throw new IllegalStateException("client went away");
        });
        service.addListener(later::incrementAndGet);

        update(ReleaseStageStatus.Success);

        assertEquals(1, later.get());
        assertEquals(ReleaseStageStatus.Success, descriptor.getStageStatusMap().get(ReleaseStage.RELEASE_PULL));
    }

    @Test
    public void removingOneListenerLeavesTheOthers() {
        AtomicInteger kept = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();
        Runnable droppedListener = dropped::incrementAndGet;
        service.addListener(kept::incrementAndGet);
        service.addListener(droppedListener);

        service.removeListener(droppedListener);
        update(ReleaseStageStatus.Success);

        assertEquals(1, kept.get());
        assertEquals(0, dropped.get());
    }
}
