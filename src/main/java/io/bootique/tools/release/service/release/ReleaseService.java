package io.bootique.tools.release.service.release;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.dto.ReleaseDescriptorDTO;
import io.bootique.tools.release.service.preferences.Preference;

public interface ReleaseService {

    Preference<String> SAVE_PATH = Preference.of("save.path", String.class);

    void createReleaseDescriptor(ReleaseDescriptor releaseDescriptor);

    ReleaseDescriptor getReleaseDescriptor();

    void rollbackPom(String repoName, ReleaseDescriptor releaseDescriptor);

    void saveRelease(Repository repository);

    void deleteLock();

    boolean hasCurrentActiveRelease();

    void nextReleaseStage();

    void nextRollbackStage();

    void createThreadForRelease();

    void prepareRelease();
}
