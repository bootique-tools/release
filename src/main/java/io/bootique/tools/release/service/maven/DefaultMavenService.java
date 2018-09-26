package io.bootique.tools.release.service.maven;

import ch.qos.logback.classic.Logger;
import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Dependency;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultMavenService implements MavenService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(MavenService.class);
    private static final String TMP_POM_FILE_NAME = ".tmp_aggregate_pom.xml";
    private static final String TMP_DEPENDENCY_FILE_NAME = ".tmp_maven_deps.output";
    private static final long TMP_DEPENDENCY_FILE_CACHE_TIME = 1200000L; // 2 min

    // [INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ bootique-test ---
    private final Pattern moduleStart = Pattern.compile("\\[INFO] --- maven-dependency-plugin:([0-9.]+):tree \\(default-cli\\) @ ([a-z0-9\\-]+) ---");
    private final Pattern moduleEnd = Pattern.compile("\\[INFO] ------------------------------------------------------------------------");

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Inject
    GitHubApi gitHubApi;

    private long depFileLastUpdateTime;
    private String depFileCache;

    @Override
    public boolean isMavenProject(Repository repository) {
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        return Files.exists(path.resolve(repository.getName()).resolve("pom.xml"));
    }

    /**
     * Check that path + ".git" directory exists
     *
     * @param path to pom.xml
     * @return root module description
     */
    @Override
    public Module resolveRootModule(Path path) {
        try {
            Document document = readDocument(path.resolve("pom.xml").toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node groupId = (Node) xpath.evaluate("/project/groupId", document, XPathConstants.NODE);
            Node artifactId = (Node) xpath.evaluate("/project/artifactId", document, XPathConstants.NODE);
            Node version = (Node) xpath.evaluate("/project/version", document, XPathConstants.NODE);
            return new Module(groupId.getTextContent(), artifactId.getTextContent(), version.getTextContent());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }
    }

    @Override
    public List<Project> getProjects(Organization organization, Predicate<Project> predicate) {
        return sort(organization
                .getRepositoryCollection().getRepositories().stream()
                .filter(this::isMavenProject)
                .map(this::createProject)
                .filter(predicate)
                .collect(Collectors.toList()));
    }

    @Override
    public List<Project> getProjectsWithoutDependencies(Organization organization, Predicate<Project> predicate) {
        return sort(organization
                .getRepositoryCollection().getRepositories().stream()
                .filter(this::isMavenProject)
                .map(this::createProjectWithoutDependencies)
                .filter(predicate)
                .collect(Collectors.toList()));
    }

    public Project createProjectWithoutDependencies(Repository repository) {
        Path basePath = preferences.get(GitService.BASE_PATH_PREFERENCE);
        Path path = basePath.resolve(repository.getName());
        return new Project(repository, path, resolveRootModule(path));
    }

    private static Document readDocument(URL url) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        try {
            DocumentBuilder domBuilder = documentBuilderFactory.newDocumentBuilder();
            try (InputStream inputStream = url.openStream()) {
                return domBuilder.parse(inputStream);
            } catch (IOException | SAXException e) {
                throw new RuntimeException("Error loading configuration from " + url, e);
            }
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method uses output from command: mvn dependency:tree -Dincludes=io.bootique
     *
     * Sample output that is parsed:
     *
     * [INFO] ------------------------------------------------------------------------
     * [INFO] Building bootique-test: unit/integration test framework for Bootique apps 0.26-SNAPSHOT
     * [INFO] ------------------------------------------------------------------------
     * [INFO]
     * [INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ bootique-test ---
     * [INFO] io.bootique:bootique-test:jar:0.26-SNAPSHOT
     * [INFO] \- io.bootique:bootique:jar:0.26-SNAPSHOT:compile
     * [INFO]
     * [INFO] ------------------------------------------------------------------------
     * [INFO] Building bootique-test-badspi-it: tests of tests with broken PolymorphicConfig 0.26-SNAPSHOT
     * [INFO] ------------------------------------------------------------------------
     * [INFO]
     * [INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ bootique-test-badspi-it ---
     * [INFO] io.bootique:bootique-test-badspi-it:jar:0.26-SNAPSHOT
     * [INFO] \- io.bootique:bootique-test:jar:0.26-SNAPSHOT:test
     * [INFO]    \- io.bootique:bootique:jar:0.26-SNAPSHOT:test
     * [INFO] ------------------------------------------------------------------------
     *
     * @param repository to create Maven project model for
     * @return maven project description
     */
    @Override
    public Project createProject(Repository repository) {
        Path basePath = preferences.get(GitService.BASE_PATH_PREFERENCE);
        Path projectPath = basePath.resolve(repository.getName());

        Module rootModule = resolveRootModule(projectPath);
        Project project = new Project(repository, projectPath, rootModule);

        String output = getAllProjectsDependencies();
        String[] strings = output.split("\n");
        if(strings.length == 0) {
            return project;
        }

        project.setModules(getSetOfModules(strings, rootModule));

        return project;
    }

    Set<Module> getSetOfModules(String[] strings, Module rootModule) {
        boolean moduleStarted = false;
        boolean firstModuleDep = false;

        Set<Module> moduleSet = new HashSet<>();
        String groupId = preferences.get(ORGANIZATION_GROUP_ID);

        Module module = null;
        for(String string: strings) {
            string = string.trim();
            if(string.isEmpty()) {
                continue;
            }
            if(moduleStarted) {
                if(firstModuleDep) {
                    if(string.contains(groupId)) {
                        String depString = string.substring("[INFO] ".length()) + ":?";
                        Dependency dep = new Dependency(depString);
                        module = dep.getModule();
                        LOGGER.debug("Start module " + module + " for dep info " + depString);
                        if (module.getGroup().equals(rootModule.getGroup())) {
                            moduleSet.add(module);
                        } else {
                            module = null;
                            moduleStarted = false;
                        }
                        firstModuleDep = false;
                    }
                } else {
                    int pos = string.indexOf("\\- " + groupId);
                    if (pos == -1) {
                        pos = string.indexOf("+- " + groupId);
                    }
                    if (pos > -1) {
                        string = string.substring(pos + 3); // strip "*- " or "\\- "
                        Dependency dependency = new Dependency(string);
                        module.addDependency(dependency);
                        LOGGER.debug("Add module " + module + " dependency " + dependency);
                    }

                    if (moduleEnd.matcher(string).matches()) {
                        moduleStarted = false;
                        LOGGER.debug("Module ended " + module);
                        module = null;
                    }
                }
            } else {
                Matcher matcher = moduleStart.matcher(string);
                if(matcher.matches()) {
                    moduleStarted = true;
                    firstModuleDep = true;
                }
            }
        }
        return moduleSet;
    }

    List<Project> sort(List<Project> projects) {
        Graph<Project> projectGraph = new Graph<>();

        Map<Module, Project> moduleToProject = new HashMap<>();
        for(Project project : projects) {
            for(Module module : project.getModules()) {
                moduleToProject.put(module, project);
            }
        }

        for(Project project : projects) {
            projectGraph.add(project);
            for(Module module: project.getModules()) {
                 for(Dependency dependency : module.getDependencies()) {
                     Project depProject = moduleToProject.get(dependency.getModule());
                     if(depProject != null && !project.equals(depProject)) {
                         dependency.getModule().setProject(depProject);
                         project.getDependencies().add(depProject);
                         projectGraph.add(project, depProject);
                     }
                 }
            }
        }

        return projectGraph.topSort();
    }

    private String getAllProjectsDependencies() {
        // get from cache
        if(depFileCache != null && System.currentTimeMillis() - depFileLastUpdateTime < TMP_DEPENDENCY_FILE_CACHE_TIME) {
            return depFileCache;
        }

        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        Path outputPath = path.resolve(TMP_DEPENDENCY_FILE_NAME);
        try {
            generateTmpAggregateProjectFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write tmp pom file", e);
        }

        String preOutput = desktopService.runMavenCommand(path, "-f", TMP_POM_FILE_NAME, "clean", "install", "-B", "-DskipTests");
        LOGGER.info("Mvn clean install: " + preOutput);
        String output = desktopService.runMavenCommand(path, "-f", TMP_POM_FILE_NAME, "dependency:tree", "-B");
        LOGGER.info(output);
        depFileCache = output;
        depFileLastUpdateTime = System.currentTimeMillis();
        OpenOption[] openOptions = {StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
        try {
            Files.write(outputPath, output.getBytes(Charset.forName("UTF-8")), openOptions);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save output to file " + outputPath, e);
        }

        return output;
    }

    /**
     * Creates aggregate pom file for all modules loaded
     */
    void generateTmpAggregateProjectFile() throws IOException {
        // mvn -f <other pom file>
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Repository> repositories = organization.getRepositoryCollection().getRepositories();

        List<String> lines = new ArrayList<>();
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        lines.add("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">");
        lines.add("    <modelVersion>4.0.0</modelVersion>");
        lines.add("    <groupId>io.bootique.tmp-parent-project</groupId>");
        lines.add("    <artifactId>bootique-tmp-parent-project</artifactId>");
        lines.add("    <version>1.0-SNAPSHOT</version>");
        lines.add("    <packaging>pom</packaging>");
        lines.add("    <modules>");
        for(Repository repository: repositories) {
            if(isMavenProject(repository)) {
                lines.add("        <module>" + repository.getName() + "</module>");
            }
        }
        lines.add("    </modules>");
        lines.add("</project>");

        OpenOption[] openOptions = {StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
        Files.write(path.resolve(TMP_POM_FILE_NAME), lines, openOptions);
    }
}