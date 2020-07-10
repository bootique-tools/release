package io.bootique.tools.release.service.console;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.service.maven.MockMavenService;
import io.bootique.tools.release.service.release.DefaultReleaseService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultConsoleReleaseServiceTest {

    private DefaultConsoleReleaseService consoleReleaseService;
    private DefaultReleaseService releaseService = new DefaultReleaseService();
    private MockMavenService mockMavenService = new MockMavenService();
    ObjectContext context;

    @BeforeEach
    void createService() {
        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();

        consoleReleaseService = new DefaultConsoleReleaseService();
        consoleReleaseService.releaseService = releaseService;
        consoleReleaseService.mavenService = mockMavenService;
    }

    @Test
    void testReadyForReleaseFromConsole() {
        Organization organization = context.newObject(Organization.class);
        assertTrue(consoleReleaseService.checkReadyForRelease("1.0.2", "1.0.2", "1.0.3", Collections.emptyList(), organization));
        assertFalse(consoleReleaseService.checkReadyForRelease("1.0.8", "1.0.4", "1.0.1", Collections.emptyList(), organization));
    }
}
