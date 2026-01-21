package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.tools.release.model.job.JobStatusResponse;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.job.BatchJobService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("job")
public class JobStatusController extends BaseJobController {

    @Inject
    private BatchJobService jobService;


    @GET
    @Path("/status")
    public JobStatusResponse getStatus() {
        BatchJob<Project, String> job = jobService.getCurrentJob();
        if (job.getDone() > 0) {
            BatchJobResult<Project, String> lastJobResult = job.getResults().get(job.getDone() - 1);
            String result = lastJobResult.result();
            Project project = lastJobResult.data();
            String branchName = project.getBranchName();
            String version = project.getVersion();
            String projectName = project.getRepository().getName();
            String status = lastJobResult.status().name();
            double progress = job.getProgress().getPercent();
            return new JobStatusResponse(projectName, status, progress, version, branchName, result);
        }
        return new JobStatusResponse(null,null,0.0,null,null,null);
    }
}

