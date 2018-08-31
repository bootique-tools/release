package io.bootique.tools.release.service.preferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultPreferenceServiceTest {

    private DefaultPreferenceService preferenceService;
    private Preference<String> TEST_PREFERENCE = Preference.of("preference.test", String.class);

    @BeforeEach
    void createService() {
        preferenceService = new DefaultPreferenceService();
    }

    @Test
    @DisplayName("Preference service test")
    void preferenceServiceTest() {
        preferenceService.set(TEST_PREFERENCE, "test");
        assertTrue(preferenceService.have(TEST_PREFERENCE));
        assertNotNull(preferenceService.get(TEST_PREFERENCE));
        assertNotNull(preferenceService.get(TEST_PREFERENCE, "test-default"));

        Preference<String> preference = Preference.of("preference.test1", String.class);

        assertThrows(PreferenceException.class, () -> preferenceService.get(preference));

        preferenceService.reset(TEST_PREFERENCE);
        assertFalse(preferenceService.have(TEST_PREFERENCE));
    }

}
