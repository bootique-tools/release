package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Dependency;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.desktop.MockDesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.MockGitHubApi;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.tools.release.util.CopyDirVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TempDirectory.class)
class DefaultMavenServiceTest {

    private MockDesktopService mockDesktopService = new MockDesktopService();
    private MockPreferenceService mockPreferenceService = new MockPreferenceService();
    private MockGitHubApi gitHubApi = new MockGitHubApi();

    private DefaultMavenService service;

    @BeforeEach
    void createService() {
        service = new DefaultMavenService();
        service.desktopService = mockDesktopService;
        service.preferences = mockPreferenceService;
    }

    @Test
    @DisplayName("Resolve root module test")
    void resolveRootModule() {
        Module module = service.resolveModule(Paths.get("."));
        assertNotNull(module);
        assertEquals("io.bootique.tools", module.getGroup());
        assertEquals("release", module.getId());
        assertEquals("1.0-SNAPSHOT", module.getVersion());
        assertTrue(module.getDependencies().isEmpty());
    }

    @Test
    @DisplayName("Get set of modules test")
    void testSetOfModules(@TempDirectory.TempDir Path destPath) throws IOException {
        Path srcPath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "bootique");
        Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, destPath);
        Module rootModule = service.resolveModule(destPath);
        Set<Module> moduleSet = service.getModules(rootModule, destPath);
        assertEquals(4, moduleSet.size());
        List<String> names = Arrays.asList("bootique-framework-parent", "bootique", "bootique-test", "bootique-test-badspi-it");
        for(Module module : moduleSet) {
            assertEquals("io.bootique", module.getGroup());
            assertEquals("0.26-SNAPSHOT", module.getVersion());
            assertTrue(names.contains(module.getId()));
        }
    }

    @Test
    @DisplayName("Create project test")
    void createProject(@TempDirectory.TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        mockPreferenceService.set(MavenService.ORGANIZATION_GROUP_ID, "io.bootique");
        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources");
        Files.walkFileTree(preparePom, new CopyDirVisitor(preparePom, path, StandardCopyOption.REPLACE_EXISTING));
        Repository repository = new Repository();
        repository.setName("bootique");
        Project project = service.createProject(repository);
        assertNotNull(project);
        assertEquals(4, project.getModules().size());

        List<String> names = Arrays.asList("bootique-framework-parent", "bootique", "bootique-test", "bootique-test-badspi-it");
        for(Module module : project.getModules()) {
            assertEquals("io.bootique", module.getGroup());
            assertEquals("0.26-SNAPSHOT", module.getVersion());
            assertTrue(names.contains(module.getId()));
        }
    }

    @Test
    void sortNoDeps() {
        List<Project> projectList = new ArrayList<>();
        Repository repository1 = new Repository();
        repository1.setName("repo1");
        Repository repository2 = new Repository();
        repository2.setName("repo2");
        Repository repository3 = new Repository();
        repository3.setName("repo3");
        Path path1 = Paths.get("/repo1/path");

        Module module1 = new Module("io.test", "module1", "1.0");
        Module module2 = new Module("io.test", "module2", "1.0");
        Module module3 = new Module("io.test", "module3", "1.0");
        Module module4 = new Module("io.test", "module4", "1.0");
        Module module5 = new Module("io.test", "module5", "1.0");

        Project project1 = new Project(repository1, path1, module1);
        project1.addModule(module1);

        Project project2 = new Project(repository2, path1, module2);
        project2.addModule(module2);
        project2.addModule(module3);

        Project project3 = new Project(repository3, path1, module4);
        project3.addModule(module4);
        project3.addModule(module5);

        projectList.add(project3);
        projectList.add(project1);
        projectList.add(project2);

        List<Project> sortedList = service.sort(projectList);
        assertEquals(3, sortedList.size());

        assertEquals(project3, sortedList.get(0));
        assertEquals(project1, sortedList.get(1));
        assertEquals(project2, sortedList.get(2));
    }

    @Test
    void sortWithDeps() {
        List<Project> projectList = new ArrayList<>();
        Repository repository1 = new Repository();
        repository1.setName("repo1");
        Repository repository2 = new Repository();
        repository2.setName("repo2");
        Repository repository3 = new Repository();
        repository3.setName("repo3");
        Path path1 = Paths.get("/repo1/path");

        Module module1 = new Module("io.test", "module1", "1.0");
        Module module2 = new Module("io.test", "module2", "1.0");
        Module module3 = new Module("io.test", "module3", "1.0");
        Module module4 = new Module("io.test", "module4", "1.0");
        Module module5 = new Module("io.test", "module5", "1.0");

        Dependency dependency1 = new Dependency("io.test", "module1", "1.0", "x");
        Dependency dependency2 = new Dependency("io.test", "module2", "1.0", "x");
        Dependency dependency3 = new Dependency("io.test", "module3", "1.0", "x");

        Project project1 = new Project(repository1, path1, module1);
        project1.addModule(module1);

        Project project2 = new Project(repository2, path1, module2);
        project2.addModule(module2);
        project2.addModule(module3);
        module2.addDependency(dependency1);

        Project project3 = new Project(repository3, path1, module4);
        project3.addModule(module4);
        project3.addModule(module5);
        module4.addDependency(dependency1);
        module4.addDependency(dependency2);
        module5.addDependency(dependency3);

        projectList.add(project3);
        projectList.add(project1);
        projectList.add(project2);

        List<Project> sortedList = service.sort(projectList);
        assertEquals(3, sortedList.size());

        assertEquals(project1, sortedList.get(0));
        assertEquals(project2, sortedList.get(1));
        assertEquals(project3, sortedList.get(2));
    }

    @Test
    @DisplayName("Get all projects test")
    void getProjectsTest(@TempDirectory.TempDir Path destPath) throws IOException {
        Path srcPath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "dummy-org-00");
        Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, destPath);
        List<Project> projects = service.getProjects(gitHubApi.getCurrentOrganization(), p -> true);
        assertEquals(3, projects.size());
    }
}