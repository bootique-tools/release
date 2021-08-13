package io.bootique.tools.release.service.release.persistent;

import io.bootique.tools.release.model.release.ReleaseDescriptor;

import java.io.IOException;

public class MockReleasePersistentService implements ReleasePersistentService {

    public boolean isReleaseSaved;

    public MockReleasePersistentService(boolean isReleaseSaved) {
        this.isReleaseSaved = isReleaseSaved;
    }

    public MockReleasePersistentService() {
        isReleaseSaved = false;
    }

    @Override
    public void saveRelease() {
    }

    @Override
    public boolean isReleaseSaved() {
        return isReleaseSaved;
    }

    @Override
    public ReleaseDescriptor loadRelease() throws IOException {
        return new ReleaseDescriptor();
    }

    @Override
    public void deleteRelease() {

    }
}
