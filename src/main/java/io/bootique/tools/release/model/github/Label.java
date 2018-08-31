package io.bootique.tools.release.model.github;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Label extends GitHubEntity implements Comparable<Label> {

    private final String name;
    private final String color;

    @JsonCreator
    public Label(@JsonProperty("name") String name, @JsonProperty("color") String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(name, label.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Label o) {
        return name.compareTo(o.name);
    }
}
