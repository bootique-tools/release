package io.bootique.tools.release.service.tasks;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.Preference;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.junit.jupiter.api.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


// todo replace test
class ReleaseSonatypeSyncTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        PreferenceService preferenceService = mock(PreferenceService.class);
        when(preferenceService.get(any(Preference.class))).thenReturn(Paths.get("."));

        MavenService mavenService = mock(MavenService.class);
        when(mavenService.isMavenProject(any(Repository.class))).thenReturn(true);

        DesktopService desktopService = mock(DesktopService.class);
        when(desktopService.runMavenCommand(any(Path.class), any(String[].class)))
                .thenReturn(TEST_MVN_OUTPUT);

        ReleaseSonatypeSyncTask sync = new ReleaseSonatypeSyncTask();
        sync.preferences = preferenceService;
        sync.mavenService = mavenService;
        sync.desktopService = desktopService;
        sync.logger = mock(LoggerService.class);
        //sync.releaseService = mock(OLD_ReleaseService.class);

        Repository repository = new Repository();
        repository.setName("bootique");

        sync.apply(repository);
    }

    private static final String TEST_MVN_OUTPUT =
            "[INFO]  * Connected to Nexus at https://oss.sonatype.org:443/, is version 2.14.20-02 and edition \"Professional\"\n" +
                    "[INFO] Getting list of available staging repositories...\n" +
                    "[INFO] \n" +
                    "[INFO] ID                   State    Description                   \n" +
                    "[INFO] iobootique-1357      OPEN     Implicitly created (auto staging).\n" +
                    "[INFO] ------------------------------------------------------------------------\n" +
                    "[INFO] Reactor Summary for bootique-framework-parent 2.0.B2-SNAPSHOT:\n" +
                    "[INFO] \n" +
                    "[INFO] bootique-framework-parent .......................... SUCCESS [  4.525 s]\n";
}