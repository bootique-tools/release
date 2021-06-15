package io.bootique.tools.release.model.persistent;

import io.bootique.tools.release.model.persistent.auto._Label;

import java.util.Objects;

public class Label extends _Label implements Comparable<Label> {

    private static final long serialVersionUID = 1L;

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
