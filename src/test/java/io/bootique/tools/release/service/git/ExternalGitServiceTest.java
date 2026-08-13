package io.bootique.tools.release.service.git;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.MockDesktopService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.runtime.CayenneRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalGitServiceTest {

    @TempDir
    Path basePath;

    private ExternalGitService gitService;
    private MockDesktopService desktopService;
    private ObjectContext context;

    @BeforeEach
    void setUp() {
        CayenneRuntime cayenneRuntime = CayenneRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();

        desktopService = new MockDesktopService();
        MockPreferenceService preferences = new MockPreferenceService();
        preferences.set(GitService.BASE_PATH_PREFERENCE, basePath);

        gitService = new ExternalGitService();
        gitService.desktopService = desktopService;
        gitService.preferenceService = preferences;
    }

    private Repository repository(String sshUrl) {
        Repository repository = context.newObject(Repository.class);
        repository.setName("bootique-jdbc");
        repository.setUrl("https://github.com/bootique/bootique-jdbc");
        repository.setSshUrl(sshUrl);
        return repository;
    }

    @Test
    public void clonesOverSshWhenSshUrlIsKnown() {
        gitService.clone(repository("git@github.com:bootique/bootique-jdbc.git"));

        assertEquals(
                List.of("git", "clone", "git@github.com:bootique/bootique-jdbc.git"),
                desktopService.lastInvocation());
    }

    /** sshUrl is nullable, and a repository imported without one must still be cloneable. */
    @Test
    public void fallsBackToHttpsWhenSshUrlIsMissing() {
        gitService.clone(repository(null));

        assertEquals(
                List.of("git", "clone", "https://github.com/bootique/bootique-jdbc"),
                desktopService.lastInvocation());
    }
}
