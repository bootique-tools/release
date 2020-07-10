package io.bootique.tools.release.model.persistent;

import io.bootique.tools.release.model.persistent.auto._Author;

import java.util.Objects;

public class Author extends _Author implements Comparable<Author> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(Author o) {
        return login.compareTo(o.login);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(login, author.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

}
