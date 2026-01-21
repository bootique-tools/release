package io.bootique.tools.release.service.tasks;

import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

public class RollbackCentralPublishTask implements ReleaseTask {

    @Inject
    ReleaseDescriptorService releaseDescriptorService;

    @Inject
    HttpTargets targets;

    @Override
    public String apply(Repository repository) {
        RepositoryDescriptor desc = releaseDescriptorService.getRepositoryDescriptorByName(repository.getName());
        if(desc.getCentralDeploymentId() == null) {
            return "skipped";
        }
        try(Response resp = targets.newTarget("central")
                .path(desc.getCentralDeploymentId())
                .request()
                .delete()) {
            if(!(resp.getStatusInfo() == Response.Status.OK)) {
                throw new RuntimeException("Can't publish release to maven central.");
            }
        }
        return "ok";
    }
}
