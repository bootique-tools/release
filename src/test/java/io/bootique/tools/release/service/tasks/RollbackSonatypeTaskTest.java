package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.DefaultMavenService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RollbackSonatypeTaskTest {

    private final MockPreferenceService mockPreferenceService = new MockPreferenceService();

    RollbackSonatypeTask rollbackSonatypeTask;

    @BeforeEach
    void setUp(@TempDir Path path) {

        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);

        rollbackSonatypeTask = new RollbackSonatypeTask();
        rollbackSonatypeTask.mavenService = new DefaultMavenService(mockPreferenceService);
        rollbackSonatypeTask.logger = mock(LoggerService.class);
        rollbackSonatypeTask.preferences = mockPreferenceService;
        rollbackSonatypeTask.desktopService = mock(DesktopService.class);
    }

    @Test
    void startTaskWithoutPom() {
        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        ObjectContext context = cayenneRuntime.newContext();

        Repository repository = context.newObject(Repository.class);
        repository.setName("");

        assertThrows(JobException.class, () -> rollbackSonatypeTask.apply(repository));
    }

    @Test
    void startTaskWithPom() throws IOException {
        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        ObjectContext context = cayenneRuntime.newContext();

        Repository repository = context.newObject(Repository.class);
        repository.setName("");

        Files.createFile(mockPreferenceService.get(
                GitService.BASE_PATH_PREFERENCE).resolve(repository.getName()).resolve("pom.xml"));

        rollbackSonatypeTask.apply(repository);
    }
}