package io.bootique.tools.release.service.validation;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ValidatePomTest {

    private DefaultValidatePomService validatePomService;
    private MockPreferenceService mockPreferenceService = new MockPreferenceService();

    @BeforeEach
    void createService() {
        validatePomService = new DefaultValidatePomService();
        validatePomService.preferences = mockPreferenceService;
    }

    @Test
    void validatePomBqVersionTest() throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource("bootique/bootique-curator");
        Path path = Path.of(Objects.requireNonNull(url).toURI());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        assertEquals(0, validatePomService.validatePom("").size());
    }

    @Test
    void validatePomFail() throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource("bootique");
        Path path = Path.of(Objects.requireNonNull(url).toURI());
        mockPreferenceService.set(GitService.BASE_PATH_PREFERENCE, path);
        List<String> validated = validatePomService.validatePom("");
        assertEquals(1, validated.size());
    }

    @Test
    void validateInvalidDependencies() {
        URL url = getClass().getClassLoader().getResource("dummy-org-00/dummy-dependencies/incorrectProject/pom.xml");
        Document document = DefaultValidatePomService.readDocument(url);
        assertFalse(validatePomService.validateDependencies(document));
    }

    @Test
    void validateValidDependencies() {
        URL url = getClass().getClassLoader().getResource("dummy-org-00/dummy-dependencies/correctProject/pom.xml");
        Document document = DefaultValidatePomService.readDocument(url);
        assertTrue(validatePomService.validateDependencies(document));
    }
}
