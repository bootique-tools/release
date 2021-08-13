package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseVersions;
import io.bootique.tools.release.service.desktop.*;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.DefaultMavenService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorServiceImpl;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;


class ReleaseSonatypeSyncTaskTest {

    private final MockPreferenceService mockPreferenceService = new MockPreferenceService();

    ReleaseSonatypeSyncTask releaseSonatypeSyncTask;
    Project project;

    @BeforeEach
    void setUp(@TempDir Path path) {

        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        ObjectContext context = cayenneRuntime.newContext();

        Repository repository = context.newObject(Repository.class);
        repository.setName("");
        project = new Project(repository, Paths.get(""), new Module());

        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(null);
        releaseDescriptorService.setRepositoryDescriptorService(new RepositoryDescriptorServiceImpl());
        releaseDescriptorService.createReleaseDescriptor(
                new ReleaseVersions("1.0.5-SNAPSHOT", "1.0.5", "1.0.6-SNAPSHOT"),
                List.of(project)
        );
        releaseSonatypeSyncTask = new ReleaseSonatypeSyncTask();

        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);

        releaseSonatypeSyncTask.preferences = mockPreferenceService;
        releaseSonatypeSyncTask.logger = mock(LoggerService.class);
        releaseSonatypeSyncTask.mavenService = new DefaultMavenService(mockPreferenceService);
    }

    @Test
    void runWithoutPom() {
        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        ObjectContext context = cayenneRuntime.newContext();

        Repository repository = context.newObject(Repository.class);
        repository.setName("");

        assertThrows(JobException.class, () -> releaseSonatypeSyncTask.apply(repository));

    }

    @Test
    void runEmptyRepoList(@TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Path pom = path.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.createFile(pom);
        }

        MockDesktopService mockDesktopService = new MockDesktopService();
        mockDesktopService.addCommandResult("");
        releaseSonatypeSyncTask.desktopService = mockDesktopService;
        assertThrows(JobException.class, () -> releaseSonatypeSyncTask.apply(project.getRepository()),
                "Staging repos not found, check release perform stage logs.");
    }

    @Test
    void runUnknownRepo(@TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Path pom = path.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.createFile(pom);
        }

        MockDesktopService mockDesktopService = new MockDesktopService();
        mockDesktopService.addCommandResult(
                "[INFO]  * Connected to Nexus at https://oss.sonatype.org:443/, is version 2.14.20-02 and edition \"Professional\"\n" +
                        "[INFO] Getting list of available staging repositories...\n" +
                        "[INFO] \n" +
                        "[INFO] ID                   State    Description                   \n" +
                        "[INFO] iobootique-1357      UNKNOWN     Implicitly created (auto staging).\n" +
                        "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] Reactor Summary for bootique-framework-parent 2.0.B2-SNAPSHOT:\n" +
                        "[INFO] \n" +
                        "[INFO] bootique-framework-parent .......................... SUCCESS [  4.525 s]\n"
        );
        releaseSonatypeSyncTask.desktopService = mockDesktopService;
        assertThrows(JobException.class, () -> releaseSonatypeSyncTask.apply(project.getRepository()),
                "Staging repo is in unknown or unsupported state." +
                        "Please go to https://oss.sonatype.org and check it manually."
        );
    }

    @Test
    void runOpenRepo(@TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Path pom = path.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.createFile(pom);
        }

        MockDesktopService mockDesktopService = new MockDesktopService();
        mockDesktopService.addCommandResult(
                "[INFO]  * Connected to Nexus at https://oss.sonatype.org:443/, is version 2.14.20-02 and edition \"Professional\"\n" +
                        "[INFO] Getting list of available staging repositories...\n" +
                        "[INFO] \n" +
                        "[INFO] ID                   State    Description                   \n" +
                        "[INFO] iobootique-1357      OPEN     Implicitly created (auto staging).\n" +
                        "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] Reactor Summary for bootique-framework-parent 2.0.B2-SNAPSHOT:\n" +
                        "[INFO] \n" +
                        "[INFO] bootique-framework-parent .......................... SUCCESS [  4.525 s]\n"
        );
        releaseSonatypeSyncTask.desktopService = mockDesktopService;
        releaseSonatypeSyncTask.apply(project.getRepository());
    }

    @Test
    void runReleasedRepo(@TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Path pom = path.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.createFile(pom);
        }

        MockDesktopService mockDesktopService = new MockDesktopService();
        mockDesktopService.addCommandResult(
                "[INFO]  * Connected to Nexus at https://oss.sonatype.org:443/, is version 2.14.20-02 and edition \"Professional\"\n" +
                        "[INFO] Getting list of available staging repositories...\n" +
                        "[INFO] \n" +
                        "[INFO] ID                   State    Description                   \n" +
                        "[INFO] iobootique-1357      RELEASED     Implicitly created (auto staging).\n" +
                        "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] Reactor Summary for bootique-framework-parent 2.0.B2-SNAPSHOT:\n" +
                        "[INFO] \n" +
                        "[INFO] bootique-framework-parent .......................... SUCCESS [  4.525 s]\n"
        );
        releaseSonatypeSyncTask.desktopService = mockDesktopService;
        releaseSonatypeSyncTask.apply(project.getRepository());
    }

//    @Test
//    void runOPenRepo(@TempDir Path path) throws IOException {
//        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
//        Path pom = path.resolve("pom.xml");
//        if (!Files.exists(pom)) {
//            Files.createFile(pom);
//        }
//
//        MockDesktopService mockDesktopService = new MockDesktopService();
//        mockDesktopService.addCommandResult(
//                "[INFO]  * Connected to Nexus at https://oss.sonatype.org:443/, is version 2.14.20-02 and edition \"Professional\"\n" +
//                        "[INFO] Getting list of available staging repositories...\n" +
//                        "[INFO] \n" +
//                        "[INFO] ID                   State    Description                   \n" +
//                        "[INFO] iobootique-1357      OPEN     Implicitly created (auto staging).\n" +
//                        "[INFO] iobootique-1367      OPEN     Implicitly created (auto staging).\n" +
//                        "[INFO] ------------------------------------------------------------------------\n" +
//                        "[INFO] Reactor Summary for bootique-framework-parent 2.0.B2-SNAPSHOT:\n" +
//                        "[INFO] \n" +
//                        "[INFO] bootique-framework-parent .......................... SUCCESS [  4.525 s]\n"
//        );
//        releaseSonatypeSyncTask.desktopService = mockDesktopService;
//        releaseSonatypeSyncTask.apply(project.getRepository());
//    }
}