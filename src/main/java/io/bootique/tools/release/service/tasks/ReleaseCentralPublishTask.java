package io.bootique.tools.release.service.tasks;

import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class ReleaseCentralPublishTask implements ReleaseTask {

    @Inject
    ReleaseDescriptorService releaseDescriptorService;

    @Inject
    HttpTargets targets;

    @Override
    public String apply(Repository repository) {
        if(releaseDescriptorService.getReleaseDescriptor()
                .getRepositoryDescriptorList()
                .stream()
                .filter(d -> d.getRepositoryName().equals(repository.getName()))
                .findFirst()
                .map(desc -> {
                    try(Response resp = targets.newTarget("central")
                            .path("/api/v1/publisher/deployment/" + desc.getCentralDeploymentId())
                            .request()
                            .post(null)) {
                        if(!(resp.getStatusInfo() == Response.Status.OK)) {
                            throw new RuntimeException("Can't publish release to maven central.");
                        }
                    }
                    return true;
                }).isPresent()) {
            return "ok";
        }
        return "skipped";
    }
}
