package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.function.BiConsumer;

public interface OutProcessor extends BiConsumer<RepositoryDescriptor, String> {
}
