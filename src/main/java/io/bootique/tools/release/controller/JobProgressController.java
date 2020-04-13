package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.value.Percent;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("progress")
public class JobProgressController extends BaseController {

    private Percent percent;
    private boolean isDone;
    private String controllerName;

    @Inject
    private BatchJobService jobService;

    @GET
    public Response progress(){
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        if(job == null){
            percent = new Percent("0");
        } else {
            isDone = job.isDone();
            controllerName = job.getBatchJobDescriptor().getControllerName();
            percent = new Percent("100");
            if (isDone){
                return Response.ok(this).build();
            }
            percent = job.getProgress();
        }
        return Response.ok(this).build();
    }

    public Percent getPercent() {
        return percent;
    }

    public String getControllerName() {
        return controllerName;
    }
}
