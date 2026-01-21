package io.bootique.tools.release.service.validation;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import jakarta.inject.Inject;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultValidatePomService implements ValidatePomService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultValidatePomService.class);
    private static final String GROUP_ID_TAG = "groupId";
    private static final String ARTIFACT_ID_TAG = "artifactId";
    private static final String VERSION_TEG = "version";
    private static final String BOOTIQUE_VERSION = "${bootique.version}";
    private static final Predicate<Path> POM_FILTER =
            ((Predicate<Path>) path1 -> path1.getFileName().toString().equals("pom.xml"))
            .and(name -> !name.toString().contains(File.pathSeparator + "target" + File.pathSeparator))
            .and(Files::isRegularFile);

    private final PreferenceService preferences;
    private final List<Function<Document, Optional<String>>> validators;

    @Inject
    public DefaultValidatePomService(PreferenceService preferences) {
        this.preferences = preferences;
        this.validators = List.of(
                this::validateDependencies,
                this::validateDependencyManagement
        );
    }

    @Override
    public Map<String, List<String>> validatePom(String repoName) {
        LOGGER.debug("Validate POM of " + repoName);
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repoName);
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(POM_FILTER)
                    .map(DefaultValidatePomService::pathToURL)
                    .map(DefaultValidatePomService::readDocument)
                    .map(pom -> Map.entry(pom.getDocumentURI(), validate(pom)))
                    .filter(e -> !e.getValue().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (IOException e) {
            throw new DesktopException("Can't process repository " + repoName, e);
        }
    }

    public List<String> validate(Document document) {
        return Collections.emptyList();
        // TODO: do we need any validations?
    }

    Optional<String> validateDependencies(Document document) {
        return validateDependencySet(document, "/project/dependencies/dependency", "dependency");
    }

    Optional<String> validateDependencyManagement(Document document) {
        return validateDependencySet(document, "/project/dependencyManagement/dependencies/dependency", "dependencyManagement");
    }

    private Optional<String> validateDependencySet(Document document, String xpath, String type) {
        String groupId = getProjectGroupId(document);
        NodeList dependenciesNodes = getNodeList(document, xpath);

        for (int i = 0; i < dependenciesNodes.getLength(); i++) {
            String bootiqueGroupId = null;
            String artifactId = null;
            String invalidVersionDefined = null;
            NodeList childNodes = dependenciesNodes.item(i).getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node item = childNodes.item(j);
                String content = item.getTextContent();
                if (GROUP_ID_TAG.equals(item.getNodeName())) {
                    if (!content.equals(groupId) && content.startsWith("io.bootique")) {
                        bootiqueGroupId = content;
                    }
                } else if(ARTIFACT_ID_TAG.equals(item.getNodeName())) {
                    artifactId = content;
                } else if (VERSION_TEG.equals(item.getNodeName())
                        && !BOOTIQUE_VERSION.equals(content)) {
                    invalidVersionDefined = content;
                }
            }
            if (bootiqueGroupId != null && invalidVersionDefined != null) {
                return Optional.of("Invalid " + type + " version " + invalidVersionDefined
                        + " for dependency " + bootiqueGroupId + ":" + artifactId);
            }
        }
        return Optional.empty();
    }

    private String getProjectGroupId(Document document) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node groupIdNode;
        try {
            groupIdNode = (Node) xpath.evaluate("/project/groupId", document, XPathConstants.NODE);
            if (groupIdNode == null) {
                groupIdNode = (Node) xpath.evaluate("/project/parent/groupId", document, XPathConstants.NODE);
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return groupIdNode.getTextContent();
    }

    static Document readDocument(URL url) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        try {
            DocumentBuilder domBuilder = documentBuilderFactory.newDocumentBuilder();
            try (InputStream inputStream = url.openStream()) {
                Document document = domBuilder.parse(inputStream);
                document.setDocumentURI(url.toString());
                return document;
            } catch (IOException | SAXException e) {
                throw new RuntimeException("Error loading configuration from " + url, e);
            }
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static NodeList getNodeList(Document document, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            throw new RuntimeException("Invalid XPath expression", ex);
        }
    }

    private static URL pathToURL(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
