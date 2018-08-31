package io.bootique.tools.release.service.preferences;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPreferenceService implements PreferenceService {

    private final Map<Preference<?>, Object> storage = new ConcurrentHashMap<>();

    @Override
    public <T> T get(Preference<T> preference) {
        @SuppressWarnings("unchecked")
        T value = (T)storage.get(preference);
        if(value == null) {
            throw new PreferenceException("No value for " + preference);
        }
        return value;
    }

    @Override
    public <T> T get(Preference<T> preference, T defaultValue) {
        @SuppressWarnings("unchecked")
        T value = (T)storage.getOrDefault(preference, defaultValue);
        return value;
    }

    @Override
    public boolean have(Preference<?> preference) {
        return storage.containsKey(preference);
    }

    @Override
    public <T> void set(Preference<T> preference, T newValue) {
        storage.put(preference, newValue);
    }

    @Override
    public void reset(Preference<?> preference) {
        storage.remove(preference);
    }

    @Override
    public String serialize() {
        return "";
    }

    @Override
    public void deserialize(String string) {
    }
}
