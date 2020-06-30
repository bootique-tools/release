package io.bootique.tools.release.model.persistent;

import io.bootique.tools.release.model.persistent.auto._User;

import java.util.Objects;

public class User extends _User implements Comparable<User> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(User o) {
        return login.compareTo(o.login);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}
