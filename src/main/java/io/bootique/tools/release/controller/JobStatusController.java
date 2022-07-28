package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("job")
public class JobStatusController extends BaseJobController {

    @Inject
    private BatchJobService jobService;

    @GET
    @Path("/status")
    public Double getStatus() {
        BatchJob<Object, Object> currentJob = jobService.getCurrentJob();
        if(currentJob!=null) {
            return jobService.getCurrentJob().getProgress().getPercent();
        }
        return null;
    }
}

