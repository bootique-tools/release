package io.bootique.tools.release.service.tasks;

import java.nio.file.Paths;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.desktop.WindowsDesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.Preference;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.ReleaseService;
import org.junit.jupiter.api.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReleaseSonatypeSyncTest {

    @Test
    public void test() {
        PreferenceService preferenceService = mock(PreferenceService.class);
        when(preferenceService.get(any(Preference.class))).thenReturn(Paths.get("D:\\_Projects\\bootique\\"));
        MavenService mavenService = mock(MavenService.class);
        when(mavenService.isMavenProject(any(Repository.class))).thenReturn(true);

        DesktopService desktopService = new WindowsDesktopService();

        ReleaseSonatypeSync sync = new ReleaseSonatypeSync();
        sync.preferences = preferenceService;
        sync.mavenService = mavenService;
        sync.desktopService = desktopService;
        sync.loggerService = mock(LoggerService.class);
        sync.releaseService = mock(ReleaseService.class);

        Repository repository = new Repository();
        repository.setName("bootique-di");

        sync.apply(repository);
    }

}