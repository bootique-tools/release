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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    @Inject
    PreferenceService preferences;

    @Override
    public List<String> validatePom(String repoName) {
        LOGGER.debug("Validate POM of " + repoName);
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repoName);
        List<String> failedPoms = new ArrayList<>();
        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(name -> (name.getFileName().toString().equals("pom.xml") &&
                            !name.toString().contains(File.pathSeparator + "target" + File.pathSeparator)))
                    .forEach(pom -> {
                        try {
                            if(!validatePom(pom)) {
                                String[] pomPathSegments = pom.toString().split(File.separator);
                                failedPoms.add(pomPathSegments[pomPathSegments.length - 2]);
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
