package io.bootique.tools.release.service.release;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;

public class MockReleaseService implements ReleaseService {

    private ReleaseDescriptor releaseDescriptor;

    @Override
    public void createReleaseDescriptor(ReleaseDescriptor releaseDescriptor) {
        this.releaseDescriptor = releaseDescriptor;
    }

    @Override
    public ReleaseDescriptor getReleaseDescriptor() {
        return releaseDescriptor;
    }

    @Override
    public void rollbackPom(String repoName, ReleaseDescriptor releaseDescriptor) {

    }

    @Override
    public void saveRelease(Repository repository) {

    }

    @Override
    public void deleteLock() {

    }

    @Override
    public boolean hasCurrentActiveRelease() {
        return true;
    }

    @Override
    public void nextReleaseStage() {

    }

    @Override
    public void nextRollbackStage() {

    }

    @Override
    public void createThreadForRelease() {

    }

    @Override
    public void prepareRelease() {

    }
}
