package io.bootique.tools.release.service.console;

import io.bootique.tools.release.service.github.MockGitHubApi;
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

    @BeforeEach
    void createService() {
        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        ObjectContext context = cayenneRuntime.newContext();

        consoleReleaseService = new DefaultConsoleReleaseService();
        consoleReleaseService.gitHubApi = new MockGitHubApi(context);
        consoleReleaseService.releaseService = releaseService;
        consoleReleaseService.mavenService = mockMavenService;
    }

    @Test
    void testReadyForReleaseFromConsole() {
        assertTrue(consoleReleaseService.checkReadyForRelease("1.0.2", "1.0.2", "1.0.3", Collections.emptyList()));
        assertFalse(consoleReleaseService.checkReadyForRelease("1.0.8", "1.0.4", "1.0.1", Collections.emptyList()));
    }
}
