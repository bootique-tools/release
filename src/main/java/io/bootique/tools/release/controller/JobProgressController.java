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
public class JobProgressController extends BaseController {

    @Inject
    private BatchJobService jobService;

    @GET
    public JobResponse progress() {
        JobResponse.Builder builder = JobResponse.builder();
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        if (job == null) {
            builder.percent(new Percent("0"));
        } else {
            builder.name(job.getBatchJobDescriptor().getControllerName())
                    .percent(new Percent("100"));
            if (job.isDone()) {
                return builder.build();
            }
            builder.percent(job.getProgress());
        }
        return builder.build();
    }
}