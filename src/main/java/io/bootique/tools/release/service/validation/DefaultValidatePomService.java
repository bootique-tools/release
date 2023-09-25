package io.bootique.tools.release.service.validation;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DefaultValidatePomService implements ValidatePomService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultValidatePomService.class);
    private static final String PROJECT_VERSION = "${project.version}";
    private static final String VERSION_TEG = "version";

    @Inject
    PreferenceService preferences;

    @Override
    public List<String> validatePom(String repoName) {
        LOGGER.debug("Validate POM of " + repoName);
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repoName);
        List<String> failedPoms = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(path)) {
            stream.filter(Files::isRegularFile)
                    .filter(name -> (name.getFileName().toString().equals("pom.xml") &&
                            !name.toString().contains(File.pathSeparator + "target" + File.pathSeparator)))
                    .forEach(pom -> {
                        try {
                            if (!validatePom(pom)) {
                                failedPoms.add("Incorrect pom: " + pom);
                            }
                            if (!validateDependencies(pom)) {
                                failedPoms.add("Incorrect dependencies definition: " + pom);
                            }
                        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
                            throw new DesktopException("Can't validate pom for " + repoName, e);
                        }
                    });
        } catch (IOException e) {
            throw new DesktopException("Can't process repository " + repoName, e);
        }

        return failedPoms;
    }

    public boolean validateDependencies(Path path) {
        Node groupId = getNode(path, "/project/groupId");
        if (groupId == null) {
            groupId = getNode(path, "/project/parent/groupId");
        }
        NodeList dependenciesNodes = getNodeList(path, "/project/dependencies/dependency");
        for (int i = 0; i < dependenciesNodes.getLength(); i++) {
            boolean isSameGroupId = false;
            boolean isVersionDefined = false;
            NodeList childNodes = dependenciesNodes.item(i).getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if ((childNodes.item(j).getTextContent().equals(groupId.getTextContent()))) {
                    isSameGroupId = true;
                }
                if (VERSION_TEG.equals(childNodes.item(j).getNodeName())) {
                    isVersionDefined = true;
                }
            }
            if (isSameGroupId && isVersionDefined) {
                return false;
            }
        }
        return true;
    }

    private static NodeList getNodeList(Path path, String expression) {
        try {
            Document document = readDocument(path.toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        } catch (MalformedURLException | XPathExpressionException ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }
    }

    private Node getNode(Path path, String expression) {
        try {
            Document document = readDocument(path.toUri().toURL());
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
        } catch (MalformedURLException | XPathExpressionException ex) {
            throw new RuntimeException("Invalid path " + path, ex);
        }
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


    private boolean validatePom(Path path) throws ParserConfigurationException, IOException,
            SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(path.toFile());

        XPath xpath = XPathFactory.newInstance().newXPath();
        Node rootGroup = (Node)xpath.evaluate("/project/groupId", document, XPathConstants.NODE);
        if(rootGroup == null) {
            return true;
        }
        String rootGroupId = rootGroup.getTextContent();
        NodeList nodeListDepManagement = (NodeList) xpath
                .evaluate("/project/dependencyManagement/dependencies/dependency",
                        document,
                        XPathConstants.NODESET);

        return validatePom(rootGroupId, nodeListDepManagement);
    }

    private boolean validatePom(String parentGroupId, NodeList nodeList) {
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node dependencyNode = nodeList.item(i);
            if(dependencyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element dependencyElement = (Element) dependencyNode;
                Node groupIdNode = dependencyElement.getElementsByTagName("groupId").item(0);
                String groupId = groupIdNode != null ?
                        groupIdNode.getTextContent() :
                        "";
                Node versionNode = dependencyElement.getElementsByTagName("version").item(0);
                String version = versionNode != null ?
                        versionNode.getTextContent() :
                        "";
                if(!processDependency(parentGroupId, groupId, version)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processDependency(String rootGroupId, String groupId, String version) {
        if(rootGroupId.equals(groupId) && !version.isEmpty()) {
            return version.equals(PROJECT_VERSION);
        }
        return true;
    }
}
