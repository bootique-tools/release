package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.ModuleDependency;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.MockDesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.MockGitHubApi;
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

    private MockDesktopService mockDesktopService = new MockDesktopService();
    private MockPreferenceService mockPreferenceService = new MockPreferenceService();
    private MockGitHubApi gitHubApi;

    private DefaultMavenService service;

    private static ObjectContext context = null;


    @BeforeEach
    void createService() {
        service = new DefaultMavenService();
        service.desktopService = mockDesktopService;
        service.preferences = mockPreferenceService;

        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();
        gitHubApi = new MockGitHubApi(context);
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
        List<Project> projectList = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Repository repository = context.newObject(Repository.class);
            repository.setName("repo" + i);
            repositories.add(repository);
        }
        Path path1 = Paths.get("/repo1/path");

        List<Module> modules = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Module module = context.newObject(Module.class);
            module.setGroupStr("io.test");
            module.setGithubId("module" + i);
            module.setVersion("1.0");
            modules.add(module);
        }

        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Project project = context.newObject(Project.class);
            project.setRepository(repositories.get(i));
            project.setPath(path1);
            if (i == 2) {
                i++;
            }
            project.setRootModule(modules.get(i));
            projects.add(project);
        }

        projects.get(0).addModule(modules.get(0));

        projects.get(1).addModule(modules.get(1));
        projects.get(1).addModule(modules.get(2));

        projects.get(2).addModule(modules.get(3));
        projects.get(2).addModule(modules.get(4));

        projectList.add(projects.get(2));
        projectList.add(projects.get(0));
        projectList.add(projects.get(1));

        List<Project> sortedList = service.sort(projectList);
        assertEquals(3, sortedList.size());

        assertEquals(projects.get(2), sortedList.get(0));
        assertEquals(projects.get(0), sortedList.get(1));
        assertEquals(projects.get(1), sortedList.get(2));
    }

    @Test
    void sortWithDeps() {
        List<Project> projectList = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Repository repository = context.newObject(Repository.class);
            repository.setName("repo" + i);
            repositories.add(repository);
        }
        Path path1 = Paths.get("/repo1/path");

        List<Module> modules = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Module module = context.newObject(Module.class);
            module.setGroupStr("io.test");
            module.setGithubId("module" + i);
            module.setVersion("1.0");
            modules.add(module);
        }

        List<ModuleDependency> dependencies = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ModuleDependency dependency = context.newObject(ModuleDependency.class);
            Module module = context.newObject(Module.class);
            module.setGroupStr("io.test");
            module.setGithubId("module" + i);
            module.setVersion("1.0");
            dependency.setModule(module);
            dependency.setType("x");
            dependencies.add(dependency);
        }

        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Project project = context.newObject(Project.class);
            project.setRepository(repositories.get(i));
            project.setPath(path1);
            if (i == 2) {
                i++;
            }
            project.setRootModule(modules.get(i));
            projects.add(project);
        }

        projects.get(0).addModule(modules.get(0));

        projects.get(1).addModule(modules.get(1));
        projects.get(1).addModule(modules.get(2));

        List<ModuleDependency> dependencyList = new ArrayList<>();
        modules.get(1).addToDependencies(dependencies.get(0));

        projects.get(2).addModule(modules.get(3));
        projects.get(2).addModule(modules.get(4));
        modules.get(3).addToDependencies(dependencies.get(0));
        modules.get(3).addToDependencies(dependencies.get(1));
        modules.get(4).addToDependencies(dependencies.get(2));

        projectList.add(projects.get(2));
        projectList.add(projects.get(0));
        projectList.add(projects.get(1));

        List<Project> sortedList = service.sort(projectList);
        assertEquals(3, sortedList.size());

        assertEquals(projects.get(0), sortedList.get(0));
        assertEquals(projects.get(1), sortedList.get(1));
        assertEquals(projects.get(2), sortedList.get(2));
    }

    @Test
    @DisplayName("Get all projects test")
    void getProjectsTest(@TempDir Path destPath) throws IOException {
        Path srcPath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "dummy-org-00");
        Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, destPath);

        Organization organization = gitHubApi.getCurrentOrganization();

        List<Project> projects = service.getProjects(organization, p -> true);
        assertEquals(3, projects.size());
    }
}