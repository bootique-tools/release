package io.bootique.tools.release.service.release.executor.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.List;
import java.util.function.Function;

public interface TaskFactory {
    Function<RepositoryDescriptor, String> createTask(Repository repository, List<ReleaseStage> executeStages);
}
