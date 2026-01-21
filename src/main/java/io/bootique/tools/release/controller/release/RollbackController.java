package io.bootique.tools.release.controller.release;

import io.bootique.di.BQInject;
import io.bootique.tools.release.controller.BaseController;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.logger.ExecutionLogger;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.executor.RollbackExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/release")
public class RollbackController extends BaseController {

    @Inject
    private ReleaseDescriptorService descriptorService;

    @Inject
    public RollbackExecutor rollbackExecutor;

    @BQInject
    @Named("rollback")
    public ExecutionLogger logger;

    @POST
    @Path("/rollback-repository")
    public void rollbackRepository(@FormParam("repository") String repositoryName, @FormParam("stage") String stage) {
        RepositoryDescriptor repositoryDescriptor = descriptorService.getRepositoryDescriptorByName(repositoryName);
        rollbackExecutor.rollbackRepository(repositoryDescriptor, ReleaseStage.valueOf(stage));
    }

    @POST
    @Path("/rollback-release")
    public void rollbackRelease() {
        rollbackExecutor.rollbackRelease();
    }

    @POST
    @Path("/skip-rollback")
    public void skip(@FormParam("repository") String repositoryName, @FormParam("stage") String stage) {
        RepositoryDescriptor repositoryDescriptor = descriptorService.getRepositoryDescriptorByName(repositoryName);
        rollbackExecutor.skip(repositoryDescriptor, ReleaseStage.valueOf(stage));
    }

    @GET
    @Path("/rollbackLog")
    public String getRollbackLogs(@QueryParam("repository") String repositoryName, @QueryParam("stage") String stage) {

        RepositoryDescriptor repositoryDescriptor = descriptorService.getRepositoryDescriptorByName(repositoryName);
        ReleaseStage releaseStage = ReleaseStage.valueOf(stage);

        return logger.getLogs(repositoryDescriptor, releaseStage);
    }

}
