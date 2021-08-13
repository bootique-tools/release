package io.bootique.tools.release.controller.release;

import io.bootique.di.BQInject;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.logger.ExecutionLogger;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.executor.ReleaseExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/release")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class ReleaseStagesController {

    @Inject
    public ReleaseDescriptorService releaseDescriptorService;

    @Inject
    public ReleaseExecutorService executionService;

    @BQInject
    @Named("release")
    public ExecutionLogger logger;

    @POST
    @Path("/skip-stage")
    public void skipRepositoryStage(@FormParam("repository") String repositoryName, @FormParam("stage") String stage) throws IOException {

        RepositoryDescriptor repositoryDescriptor = releaseDescriptorService.getRepositoryDescriptorByName(repositoryName);
        ReleaseStage repositoryStage = ReleaseStage.valueOf(stage);

        executionService.skipRepository(repositoryDescriptor, repositoryStage);
    }

    @POST
    @Path("/restart-stage")
    public void reloadRepositoryStage(@FormParam("repository") String repositoryName, @FormParam("stage") String stage) throws IOException {

        RepositoryDescriptor repositoryDescriptor = releaseDescriptorService.getRepositoryDescriptorByName(repositoryName);
        ReleaseStage repositoryStage = ReleaseStage.valueOf(stage);

        executionService.restartExecute(repositoryDescriptor, repositoryStage);
    }

    @GET
    @Path("/releaseLog")
    public String getStageLogs(@QueryParam("repository") String repositoryName, @QueryParam("stage") String stage) {

        RepositoryDescriptor repositoryDescriptor = releaseDescriptorService.getRepositoryDescriptorByName(repositoryName);
        ReleaseStage releaseStage = ReleaseStage.valueOf(stage);

        return logger.getLogs(repositoryDescriptor, releaseStage);
    }
}
