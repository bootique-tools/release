package io.bootique.tools.release.service.release;

import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

@Deprecated
class DefaultOLDReleaseServiceTest {

    private ReleaseDescriptorService releaseService;

    private MockPreferenceService mockPreferenceService = new MockPreferenceService();

    private ObjectContext context;

    @BeforeEach
    void createService() {
//        releaseService = new ReleaseDescriptorServiceImpl();
//        releaseService.objectMapper = new ObjectMapper();
//        releaseService.preferences = mockPreferenceService;

        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();

        Repository repository = context.newObject(Repository.class);
        Project project = new Project(repository, Paths.get(""), new Module());
//        OLD_ReleaseDescriptor releaseDescriptor = new OLD_ReleaseDescriptor(
//                "1.0.5-SNAPSHOT",
//                "1.0.5",
//                "1.0.6-SNAPSHOT",
//                Collections.singletonList(project),
//                ReleaseStage.RELEASE_PULL,
//                RollbackStage.NO_ROLLBACK);
//        releaseDescriptor.setLastSuccessReleaseStage(ReleaseStage.RELEASE_PULL);
//        releaseService.createReleaseDescriptor(releaseDescriptor);
    }

//    @Test
//    @DisplayName("Release save, check active and delete test.")
//    void releaseServiceTest(@TempDir Path path) throws IOException {
//        Path savePath = path.resolve(Paths.get("release-status" + File.separator + "persist"));
//        mockPreferenceService.set(OLD_ReleaseService.SAVE_PATH, savePath.toString());
//        Repository repository = context.newObject(Repository.class);
//        repository.setName("Test");
//        releaseService.saveRelease(repository);
//        assertTrue(Files.exists(Paths.get(mockPreferenceService.get(OLD_ReleaseService.SAVE_PATH),
//                releaseService.getReleaseDescriptor().getReleaseVersion(),
//                releaseService.getReleaseDescriptor().getReleaseVersion() + ".json")));
//
//        Path pathLock = Paths.get(mockPreferenceService.get(OLD_ReleaseService.SAVE_PATH),
//                "lock.txt");
//        assertTrue(Files.exists(pathLock));
//
//        List<String> lockList = Files.readAllLines(pathLock);
//        assertEquals(lockList.size(), 1);
//
//        assertTrue(releaseService.hasCurrentActiveRelease());
//
//        releaseService.deleteLock();
//
//        assertFalse(Files.exists(pathLock));
//    }
//
//    @Test
//    @DisplayName("Rollback pom test")
//    void rollbackPomTest(@TempDir Path path) throws IOException {
//        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "prepare" + File.separator + "pom.xml");
//        Path resultPom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "result" + File.separator + "pom.xml");
//        mockPreferenceService.set(OLD_ReleaseService.SAVE_PATH, path.toString());
//        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
//        Path pom = path.resolve("pom.xml");
//        if (!Files.exists(pom)) {
//            Files.createFile(pom);
//        }
//        try (FileChannel sourceChannel = new FileInputStream(preparePom.toString()).getChannel();
//             FileChannel destChannel = new FileOutputStream(pom.toString()).getChannel()) {
//            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        releaseService.rollbackPom("", releaseService.getReleaseDescriptor());
//        assertTrue(Arrays.equals(Files.readAllBytes(pom), Files.readAllBytes(resultPom)));
//    }
}
