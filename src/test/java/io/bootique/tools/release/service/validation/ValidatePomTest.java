package io.bootique.tools.release.service.validation;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.*;


public class ValidatePomTest {

    private DefaultValidatePomService validatePomService;
    private final MockPreferenceService mockPreferenceService = new MockPreferenceService();

    @BeforeEach
    void createService() {
        validatePomService = new DefaultValidatePomService(mockPreferenceService);
    }

    @Test
    void validatePomBqVersionTest() throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource("bootique/bootique-test");
        Path path = Path.of(Objects.requireNonNull(url).toURI());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Map<String, List<String>> validatePom = validatePomService.validatePom("");
        assertEquals(0, validatePom.size());
    }

    @Test
    void validatePomFail() throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource("bootique");
        Path path = Path.of(Objects.requireNonNull(url).toURI());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        Map<String, List<String>> validated = validatePomService.validatePom("");
        assertEquals(1, validated.size());
    }

    @Test
    void validateInvalidDependencies() {
        URL url = getClass().getClassLoader().getResource("dummy-org-00/dummy-dependencies/incorrectProject/pom.xml");
        assertNotNull(url);

        Document document = DefaultValidatePomService.readDocument(url);
        Optional<String> optional = validatePomService.validateDependencies(document);
        assertTrue(optional.isPresent());
        assertEquals("Invalid dependency version ${project.version} for dependency io.bootique:bootique-junit5", optional.get());
    }

    @Test
    void validateValidDependencies() {
        URL url = getClass().getClassLoader().getResource("dummy-org-00/dummy-dependencies/correctProject/pom.xml");
        assertNotNull(url);

        Document document = DefaultValidatePomService.readDocument(url);
        assertTrue(validatePomService.validateDependencies(document).isEmpty());
    }
}
