package io.bootique.tools.release.service.console;

import io.bootique.tools.release.service.github.MockGitHubApi;
import io.bootique.tools.release.service.maven.MockMavenService;
import io.bootique.tools.release.service.release.DefaultReleaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultConsoleReleaseServiceTest {

    private DefaultConsoleReleaseService consoleReleaseService;
    private MockGitHubApi mockGitHubApi = new MockGitHubApi();
    private DefaultReleaseService releaseService = new DefaultReleaseService();
    private MockMavenService mockMavenService = new MockMavenService();

    @BeforeEach
    void createService() {
        consoleReleaseService = new DefaultConsoleReleaseService();
        consoleReleaseService.gitHubApi = mockGitHubApi;
        consoleReleaseService.releaseService = releaseService;
        consoleReleaseService.mavenService = mockMavenService;
    }

    @Test
    void testReaadyForReleaseFromConsole() {
        assertTrue(consoleReleaseService.checkReadyForRelease("1.0.2", "1.0.2", "1.0.3", Collections.emptyList()));
        assertFalse(consoleReleaseService.checkReadyForRelease("1.0.8", "1.0.4", "1.0.1", Collections.emptyList()));
    }
}
