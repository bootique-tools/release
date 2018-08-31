package io.bootique.tools.release.service.preferences;

import java.util.HashMap;
import java.util.Map;

public class MockPreferenceService implements PreferenceService {

    private Map<Preference<?>, Object> map = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Preference<T> preference) {
        return (T)map.get(preference);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Preference<T> preference, T defaultValue) {
        return (T)map.getOrDefault(preference, defaultValue);
    }

    @Override
    public <T> void set(Preference<T> preference, T newValue) {
        map.put(preference, newValue);
    }

    @Override
    public void reset(Preference<?> preference) {
        map.remove(preference);
    }

    @Override
    public boolean have(Preference<?> preference) {
        return map.containsKey(preference);
    }

    @Override
    public String serialize() {
        return "";
    }

    @Override
    public void deserialize(String string) {
    }
}
