package io.bootique.tools.release.service.tasks;

import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

public class ReleaseCentralPublishTask implements ReleaseTask {

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
                .post(null)) {
            if(!(resp.getStatusInfo() == Response.Status.NO_CONTENT)) {
                throw new RuntimeException("Can't publish release to maven central.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't publish release to maven central.", e);
        }
        return "ok";
    }
}
