package io.bootique.tools.release.model.job;

public record BatchJobResult<T, R>(BatchJobStatus status, T data, R result) {
}
