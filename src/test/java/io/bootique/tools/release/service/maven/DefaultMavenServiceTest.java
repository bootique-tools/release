package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.tools.release.util.CopyDirVisitor;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import java.util.Set;

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
    void isMavenProjectTest () {
        File file = new File("src/test/resources/bootique");
        Path path = file.toPath();
        Repository repository = context.newObject(Repository.class);
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        repository.setName("bootique");
        boolean mavenProject = service.isMavenProject(repository);
        assertTrue(mavenProject);
    }

    @Test
    void getProjectPathTest(@TempDir Path path) {
        Repository repository = context.newObject(Repository.class);
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        repository.setName("test");
        Path projectPath = service.getProjectPath(repository);
        assertEquals(path + File.separator + repository.getName(), projectPath.toString());
    }

    @Test
    void getMavenCoordinatesTest() {
        File file = new File("src/test/resources/bootique");
        Path path = file.toPath();
        MavenCoordinates mavenCoordinates = service.getMavenCoordinates(path);
        assertEquals("io.bootique", mavenCoordinates.getGroupId());
        assertEquals("bootique-framework-parent", mavenCoordinates.getArtifactId());
        assertEquals("0.26-SNAPSHOT", mavenCoordinates.getVersion());
    }

    @Test
    void getDependenciesGroupIdsTest() {
        File file = new File("src/test/resources/bootique");
        Path path = file.toPath();
        Project project = new Project();
        project.setPath(path);
        Set<String> dependenciesGroupIds = service.getDependenciesGroupIds(project);
        assertEquals(12, dependenciesGroupIds.size());
    }

//    @Test
//    void syncDependencies() {
//        File file = new File("src/test/resources/mavenServiceTestProject");
//        Path path = file.toPath();
//        Project project = new Project();
//        project.setPath(path);
//        project.setGroupStr(service.getMavenCoordinates(new File("src/test/resources/mavenServiceTestProject").toPath()).getGroupId());
//
//
//        Project dependencyProject1 = new Project();
//        dependencyProject1.setGroupStr(service.getMavenCoordinates(new File("src/test/resources/mavenServiceTestProject/project1").toPath()).getGroupId());
//
//        Project dependencyProject2 = new Project();
//        dependencyProject2.setGroupStr(service.getMavenCoordinates(new File("src/test/resources/mavenServiceTestProject/project2").toPath()).getGroupId());
//
//        project.addToDependencies(dependencyProject1);
//
//        ArrayList<Project> projects = new ArrayList<>();
//        projects.add(dependencyProject1);
//        projects.add(dependencyProject2);
//
//        service.syncDependencies(project, projects);
//        assertEquals("", "");
//
//    }


    @Disabled
    @Test
    @DisplayName("Create project test")
    void createProject(@TempDir Path path) throws IOException {
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        mockPreferenceService.set(MavenService.ORGANIZATION_GROUP_ID, "io.bootique");
        Path preparePom = Paths.get("src" + File.separator + "test" + File.separator + "resources");
        Files.walkFileTree(preparePom, new CopyDirVisitor(preparePom, path, StandardCopyOption.REPLACE_EXISTING));
        Repository repository = context.newObject(Repository.class);
        repository.setName("bootique");
        Project project = service.createOrUpdateProject(repository);


        assertNotNull(project);
        assertEquals(5, project.getDependencies().size());

        List<String> names = Arrays.asList("bootique-framework-parent", "bootique",
                "bootique-test", "bootique-test-badspi-it", "bootique-curator");
        for (Project p : project.getDependencies()) {
            assertTrue(p.getGroupId().startsWith("io.bootique"));
            assertEquals("0.26-SNAPSHOT", p.getVersion());
            assertTrue(names.contains(p.getGroupId()));
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