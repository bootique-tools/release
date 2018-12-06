package io.bootique.tools.release.service.maven;

import ch.qos.logback.classic.Logger;
import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Dependency;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultMavenService implements MavenService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(MavenService.class);

    private final Pattern dependencyPattern = Pattern.compile("^io\\.bootique.*$");

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Override
    public boolean isMavenProject(Repository repository) {
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        return Files.exists(path.resolve(repository.getName()).resolve("pom.xml"));
    }

    @Override
    public List<Project> getProjects(Organization organization, Predicate<Project> predicate) {
        return sort(organization
                .getRepositoryCollection().getRepositories()
                .stream()
                .filter(this::isMavenProject)
                .map(this::createProject)
                .collect(Collectors.toList()))
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
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
     * @param repository to create Maven project model for
     * @return maven project description
     */
    @Override
    public Project createProject(Repository repository) {
        Path basePath = preferences.get(GitService.BASE_PATH_PREFERENCE);
        Path projectPath = basePath.resolve(repository.getName());

        Module rootModule = resolveModule(projectPath);
        Project project = new Project(repository, projectPath, rootModule);
        project.setModules(getModules(rootModule, projectPath));

        return project;
    }

    Set<Module> getModules(Module rootModule, Path path) {
        Set<Module> moduleSet = new HashSet<>();
        try {
            Document document = readDocument(path.resolve("pom.xml").toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList dependencies = (NodeList) xpath.evaluate("/project/dependencies/dependency", document, XPathConstants.NODESET);
            for(int i = 0; i < dependencies.getLength(); i++) {
                Element element = (Element) dependencies.item(i);
                String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                if(dependencyPattern.matcher(groupId).matches()) {
                    String artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
                    NodeList typeNodes = element.getElementsByTagName("scope");
                    Dependency dependency = new Dependency(groupId,
                            artifactId,
                            rootModule.getVersion(),
                            typeNodes.getLength() != 0 ? typeNodes.item(0).getTextContent() : null);
                    rootModule.addDependency(dependency);
                }
            }
            NodeList modules = (NodeList) xpath.evaluate("/project/modules/module", document, XPathConstants.NODESET);
            for(int i = 0; i < modules.getLength(); i++) {
                Path currPath = path.resolve(modules.item(i).getTextContent());
                Module currModule = resolveModule(currPath);
                moduleSet.addAll(getModules(currModule, currPath));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }

        moduleSet.add(rootModule);
        return moduleSet;
    }

    /**
     * Check that path + ".git" directory exists
     *
     * @param path to pom.xml
     * @return root module description
     */
    @Override
    public Module resolveModule(Path path) {
        try {
            Document document = readDocument(path.resolve("pom.xml").toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node groupId = (Node) xpath.evaluate("/project/groupId", document, XPathConstants.NODE);
            Node artifactId = (Node) xpath.evaluate("/project/artifactId", document, XPathConstants.NODE);
            Node version = (Node) xpath.evaluate("/project/version", document, XPathConstants.NODE);
            if(groupId == null || version == null) {
                groupId = (Node) xpath.evaluate("/project/parent/groupId", document, XPathConstants.NODE);
                version = (Node) xpath.evaluate("/project/parent/version", document, XPathConstants.NODE);
            }
            return new Module(groupId.getTextContent(), artifactId.getTextContent(), version.getTextContent());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }
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
}