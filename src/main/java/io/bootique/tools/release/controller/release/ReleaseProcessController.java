package io.bootique.tools.release.controller.release;

import io.bootique.tools.release.controller.BaseController;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.executor.ReleaseExecutor;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/release")
public class ReleaseProcessController extends BaseController {

    @Inject
    public ReleaseDescriptorService releaseDescriptorService;

    @Inject
    public ReleaseExecutor releaseExecutor;

    @GET
    @Path("/stage")
    public List<String> getReleaseStages() {

        ReleaseStage[] releaseStages = ReleaseStage.values();

        return Arrays.stream(releaseStages)
                .filter(stage -> stage != ReleaseStage.NO_RELEASE)
                .map(Enum::toString)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/repository")
    public List<RepositoryDescriptor> getRepository() {
        ReleaseDescriptor descriptor = releaseDescriptorService.getReleaseDescriptor();
        return descriptor == null ? List.of() : descriptor.getRepositoryDescriptorList();
    }

    @POST
    @Path("/start-execute")
    public void startExecute() {
        releaseExecutor.executeRelease();
    }

    @POST
    @Path("/start-sync-stage")
    public void startSyncStage() {
        releaseExecutor.startSyncStage();
    }

    @POST
    @Path("/drop")
    public void dropRelease() {
        releaseDescriptorService.dropReleaseDescriptor();
    }
}


