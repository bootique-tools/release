package io.bootique.tools.release.service.tasks;


import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseVersions;
import io.bootique.tools.release.service.desktop.*;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.tools.release.service.release.ReleaseDescriptorFactory;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorServiceImpl;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;


class RollbackMvnGitTaskTest {

    private final MockPreferenceService mockPreferenceService = new MockPreferenceService();

    RollbackMvnGitTask rollbackMvnGitTask;
    Project project;

    @BeforeEach
    void setUp() {

        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        ObjectContext context = cayenneRuntime.newContext();

        Repository repository = context.newObject(Repository.class);
        repository.setName("");
        project = new Project();
        project.setRepository(repository);

        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(null);
        releaseDescriptorService.setRepositoryDescriptorService(new RepositoryDescriptorServiceImpl());
        releaseDescriptorService.createReleaseDescriptor(
                new ReleaseVersions("1.0.5-SNAPSHOT", "1.0.5", "1.0.6-SNAPSHOT"),
                List.of(project)
        );
        rollbackMvnGitTask = new RollbackMvnGitTask();

        rollbackMvnGitTask.preferences = mockPreferenceService;
        rollbackMvnGitTask.loggerService = mock(LoggerService.class);
        rollbackMvnGitTask.desktopService = createDesktopService();
        rollbackMvnGitTask.gitService = mock(GitService.class);
        rollbackMvnGitTask.releaseDescriptorService = releaseDescriptorService;
    }


    private DesktopService createDesktopService() {
        String os = System.getProperty("os.name").toLowerCase();
        String javaHome = "/usr/libexec/java_home -v 11";
        if (os.contains("win")) {
            return new WindowsDesktopService(javaHome);
        }
        if (os.contains("mac")) {
            return new MacOSService(javaHome);
        }
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxDesktopService(javaHome);
        }
        return new GenericDesktopService(javaHome);
    }

    @Test
    void rollbackPomTest(@TempDir Path path) throws IOException {
        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "prepare" + File.separator + "pom.xml");
        Path resultPom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "result" + File.separator + "pom.xml");

        mockPreferenceService.set(ReleasePersistentService.SAVE_PATH, path.toString());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);

        Path pom = path.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.createFile(pom);
        }
        try (FileChannel sourceChannel = new FileInputStream(preparePom.toString()).getChannel();
             FileChannel destChannel = new FileOutputStream(pom.toString()).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ReleaseDescriptorFactory releaseDescriptorFactory = new ReleaseDescriptorFactory();

        ReleaseDescriptor releaseDescriptor = releaseDescriptorFactory.createNotSyncDescriptor(3);
        releaseDescriptor.setReleaseVersions(
                new ReleaseVersions("1.0.6-SNAPSHOT", "1.0.6-SNAPSHOT", "1.0.5-SNAPSHOT")
        );

        rollbackMvnGitTask.rollbackPom("", releaseDescriptor);
        assertArrayEquals(Files.readAllBytes(pom), Files.readAllBytes(resultPom));
    }

    @Test
    void runTaskTest(@TempDir Path path) throws IOException {
        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "rollbackPom" + File.separator + "prepare" + File.separator + "pom.xml");

        mockPreferenceService.set(ReleasePersistentService.SAVE_PATH, path.toString());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);

        Path pom = path.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.createFile(pom);
        }
        try (FileChannel sourceChannel = new FileInputStream(preparePom.toString()).getChannel();
             FileChannel destChannel = new FileOutputStream(pom.toString()).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        rollbackMvnGitTask.apply(project.getRepository());
    }
}