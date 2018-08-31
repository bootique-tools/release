package io.bootique.tools.release.service.preferences;

public interface PreferenceService {

    <T> T get(Preference<T> preference);

    <T> T get(Preference<T> preference, T defaultValue);

    <T> void set(Preference<T> preference, T newValue);

    void reset(Preference<?> preference);

    boolean have(Preference<?> preference);

    String serialize();

    void deserialize(String string);
}
