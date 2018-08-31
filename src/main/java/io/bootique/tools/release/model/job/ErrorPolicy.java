package io.bootique.tools.release.model.job;

public enum ErrorPolicy {
    RETRY_ON_ERROR,
    SKIP_ON_ERROR,
    ABORT_ALL_ON_ERROR
}
