package io.bootique.tools.release.service.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.MockGitHubApi;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TempDirectory.class)
class DefaultReleaseServiceTest {

    private DefaultReleaseService releaseService;

    private MockPreferenceService mockPreferenceService = new MockPreferenceService();

    private MockGitHubApi mockGitHubApi = new MockGitHubApi();

    @BeforeEach
    void createService() {
        releaseService = new DefaultReleaseService();
        releaseService.objectMapper = new ObjectMapper();
        releaseService.preferences = mockPreferenceService;
        Repository repository = new Repository();
        Project project = new Project(repository, Paths.get(""), new Module());
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor(
                "1.0.5-SNAPSHOT",
                "1.0.5",
                "1.0.6-SNAPSHOT",
                Collections.singletonList(project),
                ReleaseStage.RELEASE_PULL,
                RollbackStage.NO_ROLLBACK,
                false
        );
        releaseService.createReleaseDescriptor(releaseDescriptor);
        releaseService.gitHubApi = mockGitHubApi;
    }

    @Test
    @DisplayName("Release save, check active and delete test.")
    void releaseServiceTest(@TempDirectory.TempDir Path path) throws IOException {
        Path savePath = path.resolve(Paths.get("release-status" + File.separator + "persist"));
        mockPreferenceService.set(ReleaseService.SAVE_PATH, savePath.toString());
        releaseService.saveRelease();
        assertTrue(Files.exists(Paths.get(mockPreferenceService.get(ReleaseService.SAVE_PATH),
                releaseService.getReleaseDescriptor().getReleaseVersion(),
                releaseService.getReleaseDescriptor().getReleaseVersion() + ".txt")));

        Path pathLock = Paths.get(mockPreferenceService.get(ReleaseService.SAVE_PATH),
                "lock.txt");
        assertTrue(Files.exists(pathLock));

        List<String> lockList = Files.readAllLines(pathLock);
        assertEquals(lockList.size(), 1);

        assertTrue(releaseService.hasCurrentActiveRelease());

        releaseService.deleteLock();

        assertFalse(Files.exists(pathLock));
    }

    @Test
    @DisplayName("Rollback pom test")
    void rollbackPomTest(@TempDirectory.TempDir Path path) throws IOException {
        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "prepare" + File.separator + "pom.xml");
        Path resultPom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "result" + File.separator + "pom.xml");
        mockPreferenceService.set(ReleaseService.SAVE_PATH, path.toString());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Path pom = path.resolve("pom.xml");
        if(!Files.exists(pom)) {
            Files.createFile(pom);
        }
        try (FileChannel sourceChannel = new FileInputStream(preparePom.toString()).getChannel();
             FileChannel destChannel = new FileOutputStream(pom.toString()).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        releaseService.rollbackPom("", releaseService.getReleaseDescriptor());
        assertTrue(Arrays.equals(Files.readAllBytes(pom), Files.readAllBytes(resultPom)));
    }
}
