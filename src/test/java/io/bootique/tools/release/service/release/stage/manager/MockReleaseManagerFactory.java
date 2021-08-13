package io.bootique.tools.release.service.release.stage.manager;

import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;

public class MockReleaseManagerFactory {

    public StageManagerService createStageManager(ReleaseDescriptor releaseDescriptor) {
        ReleaseDescriptorServiceImpl releaseDescriptorService = new ReleaseDescriptorServiceImpl(null);
        releaseDescriptorService.setReleaseDescriptor(releaseDescriptor);

        StageManagerImplService managerImplService = new StageManagerImplService();
        managerImplService.releaseDescriptorService = releaseDescriptorService;

        return managerImplService;
    }
}
