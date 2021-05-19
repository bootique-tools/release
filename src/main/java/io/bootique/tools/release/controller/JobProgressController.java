package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobResponse;
import io.bootique.value.Percent;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/progress")
public class JobProgressController {

    @Inject
    private BatchJobService jobService;

    @GET
    public JobResponse<Repository, String> progress() {
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        return job == null
                ? JobResponse.<Repository, String>builder().percent(new Percent("0")).build()
                : JobResponse.<Repository, String>builder().percent(job.getProgress())
                    .name(job.getBatchJobDescriptor().getControllerName())
                    .build();
    }
}