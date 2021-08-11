package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.ExternalGitService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.Preference;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReleasePullTaskTest {

    ReleasePullTask pullTask;

    @BeforeEach
    public void setUp() {
        PreferenceService preferenceService = mock(PreferenceService.class);
        DesktopService desktopService = mock(DesktopService.class);

        ExternalGitService gitService = new ExternalGitService();
        //gitService.desktopService = desktopService;
        //gitService.preferenceService = preferenceService;

        pullTask = new ReleasePullTask();
        pullTask.logger = mock(LoggerService.class);
        pullTask.gitService = gitService;
    }

    @Test
    void prepareService() {

        Repository repository = new Repository();
        repository.setName("bootique");

        pullTask.apply(repository);

    }
}