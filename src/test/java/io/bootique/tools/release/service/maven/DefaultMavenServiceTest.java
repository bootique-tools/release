package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.ModuleDependency;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.tools.release.util.CopyDirVisitor;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMavenServiceTest {

    private MockPreferenceService mockPreferenceService;
    private ObjectContext context;
    private DefaultMavenService service;

    @BeforeEach
    void createService() {
        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();
        mockPreferenceService = new MockPreferenceService();
        service = new DefaultMavenService(mockPreferenceService);
    }

    @Test
    @DisplayName("Resolve root module test")
    void resolveRootModule() {
        Module module = service.resolveModule(Paths.get("."));
        assertNotNull(module);
        assertEquals("io.bootique.tools", module.getGroupStr());
        assertEquals("release", module.getGithubId());
        assertEquals("1.0-SNAPSHOT", module.getVersion());
        assertTrue(module.getDependencies().isEmpty());
    }

    @Test
    @DisplayName("Get set of modules test")
    void testSetOfModules(@TempDir Path destPath) throws IOException {
        Path srcPath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "bootique");
        Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, destPath);
        Module rootModule = service.resolveModule(destPath);
        rootModule.setObjectContext(context);
        List<Module> moduleList = service.getModules(rootModule, destPath);
        assertEquals(5, moduleList.size());
        List<String> names = Arrays.asList("bootique-framework-parent", "bootique",
                "bootique-test", "bootique-test-badspi-it", "bootique-curator");
        for(Module module : moduleList) {
            assertTrue(module.getGroupStr().startsWith("io.bootique"));
            assertEquals("0.26-SNAPSHOT", module.getVersion());
            assertTrue(names.contains(module.getGithubId()));
        }
    }

    @Test
    @DisplayName("Create project test")
    void createProject(@TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        mockPreferenceService.set(MavenService.ORGANIZATION_GROUP_ID, "io.bootique");
        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources");
        Files.walkFileTree(preparePom, new CopyDirVisitor(preparePom, path, StandardCopyOption.REPLACE_EXISTING));
        Repository repository = context.newObject(Repository.class);
        repository.setName("bootique");
        Project project = service.createProject(repository);
        assertNotNull(project);
        assertEquals(5, project.getModules().size());

        List<String> names = Arrays.asList("bootique-framework-parent", "bootique",
                "bootique-test", "bootique-test-badspi-it", "bootique-curator");
        for(Module module : project.getModules()) {
            assertTrue(module.getGroupStr().startsWith("io.bootique"));
            assertEquals("0.26-SNAPSHOT", module.getVersion());
            assertTrue(names.contains(module.getGithubId()));
        }
    }

    @Test
    void sortNoDeps() {
        List<Repository> repositories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Repository repository = context.newObject(Repository.class);
            repository.setName("repo" + i);
            repositories.add(repository);
        }

        Path path1 = Paths.get("/repo1/path");
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Project project = context.newObject(Project.class);
            project.setRepository(repositories.get(i));
            project.setPath(path1);
            projects.add(project);
        }

        List<Project> projectList = new ArrayList<>();
        projectList.add(projects.get(2));
        projectList.add(projects.get(0));
        projectList.add(projects.get(1));

        List<Project> sortedList = service.sortProjects(projectList);
        assertEquals(3, sortedList.size());

        assertEquals(projects.get(2), sortedList.get(0));
        assertEquals(projects.get(0), sortedList.get(1));
        assertEquals(projects.get(1), sortedList.get(2));
    }

    @Test
    void sortWithDeps() {
        List<Repository> repositories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Repository repository = context.newObject(Repository.class);
            repository.setName("repo" + i);
            repositories.add(repository);
        }

        Path path1 = Paths.get("/repo1/path");
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Project project = context.newObject(Project.class);
            project.setRepository(repositories.get(i));
            project.setPath(path1);
            projects.add(project);
        }

        projects.get(1).addToDependencies(projects.get(0));
        projects.get(2).addToDependencies(projects.get(1));

        List<Project> projectList = new ArrayList<>();
        projectList.add(projects.get(2));
        projectList.add(projects.get(0));
        projectList.add(projects.get(1));

        List<Project> sortedList = service.sortProjects(projectList);
        assertEquals(3, sortedList.size());

        assertEquals(projects.get(0), sortedList.get(0));
        assertEquals(projects.get(1), sortedList.get(1));
        assertEquals(projects.get(2), sortedList.get(2));
    }
}