package io.bootique.tools.release.util;

public class RequestCache<T> {

    private final T object;

    public RequestCache(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }
}
