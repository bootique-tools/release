package io.bootique.tools.release.service.preferences;

import java.util.Objects;

public class Preference<T> {

    private final String name;
    private final Class<T> type;

    public static <T> Preference<T> of(String name, Class<T> type) {
        return new Preference<>(Objects.requireNonNull(name), Objects.requireNonNull(type));
    }

    private Preference(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Preference<?> that = (Preference<?>) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
