package io.bootique.tools.release.service.validation;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
        assertEquals(1, validatePomService.validatePom("").size());
    }
}
