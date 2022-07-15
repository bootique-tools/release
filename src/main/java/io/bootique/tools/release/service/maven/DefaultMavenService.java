package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.maven.persistent.auto._Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultMavenService implements MavenService {

    private final PreferenceService preferences;
    private static final String POM_XML = "pom.xml";

    @Inject
    GitService gitService;

    @Inject
    public DefaultMavenService(PreferenceService preferences) {
        this.preferences = preferences;
    }


    @Override
    public boolean isMavenProject(Repository repository) {
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        return Files.exists(path.resolve(repository.getName()).resolve(POM_XML));
    }


    @Override
    public Project createOrUpdateProject(Repository repository) {

        ObjectContext context = repository.getObjectContext();
        Project project = getProject(repository, context);
        Path projectPath = getProjectPath(repository);
        MavenCoordinates mavenCoordinates = getMavenCoordinates(projectPath);

        project.setGroupStr(mavenCoordinates.getGroupId());
        project.setVersion(mavenCoordinates.getVersion());
        project.setRepository(repository);
        project.setPath(projectPath);
        project.setBranchName(gitService.getCurrentBranchName(repository));
        return project;
    }

    Path getProjectPath(Repository repository) {
        Path basePath = preferences.get(GitService.BASE_PATH_PREFERENCE);
        return basePath.resolve(repository.getName());
    }

    private Project getProject(Repository repository, ObjectContext context) {
        Project project = ObjectSelect.query(Project.class).where(_Project.REPOSITORY.eq(repository)).selectFirst(context);
        if (project == null) {
            project = context.newObject(Project.class);
        }
        return project;
    }

    MavenCoordinates getMavenCoordinates(Path path) {
        MavenCoordinates coordinates = new MavenCoordinates();
        try {
            Document document = readDocument(path.resolve(POM_XML).toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();

            Node groupId = (Node) xpath.evaluate("/project/groupId", document, XPathConstants.NODE);
            Node artifactId = (Node) xpath.evaluate("/project/artifactId", document, XPathConstants.NODE);
            Node version = (Node) xpath.evaluate("/project/version", document, XPathConstants.NODE);
            if (groupId == null || version == null) {
                groupId = (Node) xpath.evaluate("/project/parent/groupId", document, XPathConstants.NODE);
                version = (Node) xpath.evaluate("/project/parent/version", document, XPathConstants.NODE);
            }
            coordinates.setGroupId(groupId.getTextContent());
            coordinates.setArtifactId(artifactId.getTextContent());
            coordinates.setVersion(version.getTextContent());
        } catch (XPathExpressionException | MalformedURLException e) {
            e.printStackTrace();
        }
        return coordinates;
    }

    public void syncDependencies(Project project, List<Project> projects) {
        Set<String> allDependenciesGroupIds = getDependenciesGroupIds(project);
        deleteRedundantDependencies(project, allDependenciesGroupIds);
        addMissingDependencies(project, projects, allDependenciesGroupIds);

    }

    private void deleteRedundantDependencies(Project project, Set<String> allDependenciesGroupIds) {
        List<Project> dependenciesToRemove = new ArrayList<>();
        for (Project projectDependency : project.getDependencies()) {
            if (!allDependenciesGroupIds.contains(projectDependency.getGroupStr())) {
                dependenciesToRemove.add(projectDependency);
            }
        }
        for (Project dependency : dependenciesToRemove) {
            project.removeFromDependencies(dependency);
        }
    }

    private void addMissingDependencies(Project project, List<Project> projects, Set<String> allDependenciesGroupIds) {
        for (String groupId : allDependenciesGroupIds) {
            for (Project currentProject : projects) {
                if (currentProject.getGroupStr().equals(groupId) && !currentProject.equals(project)) {
                    project.addToDependencies(currentProject);
                }
            }
        }
    }


    Set<String> getDependenciesGroupIds(Project project) {
        Set<Path> paths = getModulesPaths(project);
        Set<String> allDependenciesGroupIds = new HashSet<>();
        for (Path path : paths) {
            NodeList dependenciesNodes = getNodeList(path, "/project/dependencies/dependency");
            for (int i = 0; i < dependenciesNodes.getLength(); i++) {
                Element element = (Element) dependenciesNodes.item(i);
                String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                allDependenciesGroupIds.add(groupId);
            }
        }
        return allDependenciesGroupIds;
    }

     private Set<Path> getModulesPaths(Project project) {
        Set<Path> paths = new HashSet<>();
        Path path = project.getPath();
        NodeList modules = getNodeList(path, "/project/modules/module");
        for (int i = 0; i < modules.getLength(); i++) {
            Path currPath = path.resolve(modules.item(i).getTextContent());
            if (Files.exists(currPath.resolve(POM_XML))) {
                paths.add(path);
            }
            paths.add(currPath);
        }
        return paths;
    }

    private NodeList getNodeList(Path path, String expression) {
        try {
            Document document = readDocument(path.resolve(POM_XML).toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        } catch (MalformedURLException | XPathExpressionException ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }
    }

    @Override
    public List<Project> sortProjects(List<Project> projects) {
        Graph<Project> projectGraph = new Graph<>();
        for (Project project : projects) {
            projectGraph.add(project);
            for (Project dependency : project.getDependencies()) {
                projectGraph.add(project, dependency);
            }
        }
        return projectGraph.topSort();
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
}





