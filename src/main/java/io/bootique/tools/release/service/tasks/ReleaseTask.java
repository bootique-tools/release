package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;

import java.util.function.Function;

public interface ReleaseTask extends Function<Repository, String> {
}
