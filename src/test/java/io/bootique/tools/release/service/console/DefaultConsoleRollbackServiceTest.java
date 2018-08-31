package io.bootique.tools.release.service.console;

import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.release.MockReleaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultConsoleRollbackServiceTest {

    private DefaultConsoleRollbackService rollbackService;
    private MockReleaseService releaseService = new MockReleaseService();

    @BeforeEach
    void createService() {
        rollbackService = new DefaultConsoleRollbackService();
        rollbackService.releaseService = releaseService;
    }

    @Test
    void testReaadyForReleaseFromConsole() {
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor(
                "1.0.5-SNAPSHOT",
                "1.0.5",
                "1.0.6-SNAPSHOT",
                Collections.singletonList(new Project()),
                ReleaseStage.RELEASE_PULL,
                RollbackStage.NO_ROLLBACK,
                false
        );
        releaseService.createReleaseDescriptor(releaseDescriptor);
        assertTrue(rollbackService.checkReadyForRollback());
    }

    @Test
    void notReadyForRelease(){
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor(
                "1.0.5-SNAPSHOT",
                "1.0.5",
                "1.0.6-SNAPSHOT",
                Collections.singletonList(new Project()),
                ReleaseStage.RELEASE_SYNC,
                RollbackStage.NO_ROLLBACK,
                false
        );
        releaseService.createReleaseDescriptor(releaseDescriptor);
        assertFalse(rollbackService.checkReadyForRollback());
    }
}
