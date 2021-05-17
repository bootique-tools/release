package io.bootique.tools.release.service.maven;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.ModuleDependency;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.PreferenceService;
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
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultMavenService implements MavenService {

    private final Pattern dependencyPattern;

    private final PreferenceService preferences;

    private final Map<String, Module> moduleMap;

    @Inject
    public DefaultMavenService(PreferenceService preferences) {
        this.preferences = preferences;
        this.dependencyPattern = Pattern.compile(this.preferences.get(MavenService.GROUP_ID_PATTERN) + ".*$");
        this.moduleMap = new HashMap<>();
    }

    @Override
    public boolean isMavenProject(Repository repository) {
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        return Files.exists(path.resolve(repository.getName()).resolve("pom.xml"));
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
        moduleMap.put(rootModule.getGithubId(), rootModule);
        Project project = new Project(repository, projectPath, rootModule);
        project.setModules(getModules(rootModule, projectPath));

        return project;
    }

    List<Module> getModules(Module rootModule, Path path) {
        List<Module> moduleList = new ArrayList<>();
        try {
            Document document = readDocument(path.resolve("pom.xml").toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList dependencies = (NodeList) xpath.evaluate("/project/dependencies/dependency", document, XPathConstants.NODESET);
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element element = (Element) dependencies.item(i);
                String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                if (dependencyPattern.matcher(groupId).matches()) {
                    String artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
                    NodeList typeNodes = element.getElementsByTagName("scope");
                    ModuleDependency dependency;
                    if (this.moduleMap.containsKey(artifactId)) {
                        dependency = new ModuleDependency(this.moduleMap.get(artifactId),
                                typeNodes.getLength() != 0 ? typeNodes.item(0).getTextContent() : null
                                , rootModule.getObjectContext()
                        );
                    } else {
                        dependency = new ModuleDependency(groupId,
                                artifactId,
                                rootModule.getVersion(),
                                typeNodes.getLength() != 0 ? typeNodes.item(0).getTextContent() : null
                                , rootModule.getObjectContext()
                        );
                        this.moduleMap.put(artifactId, dependency.getModule());
                    }

                    rootModule.addToDependencies(dependency);
                }
            }
            NodeList modules = (NodeList) xpath.evaluate("/project/modules/module", document, XPathConstants.NODESET);
            for (int i = 0; i < modules.getLength(); i++) {
                Path currPath = path.resolve(modules.item(i).getTextContent());
                Module currModule = resolveModule(currPath);
                rootModule.getObjectContext().registerNewObject(currModule);
                moduleList.addAll(getModules(currModule, currPath));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }

        moduleList.add(rootModule);
        return moduleList;
    }

    /**
     * Check that path + ".git" directory exists
     *
     * @param path to pom.xml
     * @return root module description
     */
    Module resolveModule(Path path) {
        try {
            Document document = readDocument(path.resolve("pom.xml").toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node groupId = (Node) xpath.evaluate("/project/groupId", document, XPathConstants.NODE);
            Node artifactId = (Node) xpath.evaluate("/project/artifactId", document, XPathConstants.NODE);
            Node version = (Node) xpath.evaluate("/project/version", document, XPathConstants.NODE);
            if (groupId == null || version == null) {
                groupId = (Node) xpath.evaluate("/project/parent/groupId", document, XPathConstants.NODE);
                version = (Node) xpath.evaluate("/project/parent/version", document, XPathConstants.NODE);
            }
            Module module;
            if (this.moduleMap.containsKey(artifactId.getTextContent())) {
                module = this.moduleMap.get(artifactId.getTextContent());
                module.setGroupStr(groupId.getTextContent());
                module.setVersion(version.getTextContent());
            } else {
                module = new Module(groupId.getTextContent(), artifactId.getTextContent(), version.getTextContent());
                this.moduleMap.put(artifactId.getTextContent(), module);
            }
            return module;
        } catch (Exception ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }
    }

    @Override
    public List<Project> sortProjects(List<Project> projects) {
        Graph<Project> projectGraph = new Graph<>();
        for (Project project : projects) {
            projectGraph.add(project);
            for(Project dependency : project.getDependencies()) {
                projectGraph.add(project, dependency);
            }
        }
        return projectGraph.topSort();
    }
}