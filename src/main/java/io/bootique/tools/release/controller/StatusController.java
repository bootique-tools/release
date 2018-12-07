package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.tools.release.model.job.BatchJobStatus;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.job.BatchForkJoinTask;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobRespnse;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.view.ReleaseStatusMsg;
import io.bootique.value.Percent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/release/process")
public class StatusController extends BaseController{

    @Inject
    private ReleaseService releaseService;

    @Inject
    private BatchJobService jobService;

    @GET
    @Path("/has-descriptor")
    @Produces(MediaType.APPLICATION_JSON)
    public ReleaseDescriptor hasDescriptor() {
        return releaseService.getReleaseDescriptor();
    }

    @GET
    @Path("/status")
    public JobRespnse status() {
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        if(job == null){
            return null;
        }
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        List<BatchJobResult<Repository, String>> jobResults = new ArrayList<>();
        if(releaseDescriptor != null &&
                job.getTotal() != releaseDescriptor.getProjectList().size() &&
                releaseDescriptor.getCurrentReleaseStage() != ReleaseStage.NO_RELEASE) {
            List<Repository> repositories = releaseDescriptor.getProjectList().stream()
                    .map(Project::getRepository)
                    .collect(Collectors.toList());
            int index = repositories.indexOf(job.getResults().get(0).getData());
            repositories.subList(0, index).forEach(repository ->
                    jobResults.add(new BatchJobResult<>(BatchJobStatus.SUCCESS, repository, "")));
        }
        jobResults.addAll(job.getResults());
        return releaseDescriptor == null ?
                new JobRespnse<>(job.getProgress(), job.getResults()) :
                new JobRespnse<>(getProgress(job.getDone(), releaseDescriptor.getProjectList().size(), job.getTotal()),
                        jobResults, releaseDescriptor.getCurrentReleaseStage() != ReleaseStage.NO_RELEASE ?
                        releaseDescriptor.getCurrentReleaseStage().getText() : releaseDescriptor.getCurrentRollbackStage().getText());
    }

    private Percent getProgress(int done, int all, int total) {
        return new Percent(((double)done + all - total) / all);
    }

    @GET
    @Path("/need-to-close")
    public boolean needToCloseRelease(){
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        if(job == null){
            return false;
        }
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        ReleaseStage releaseStage = releaseDescriptor.getCurrentReleaseStage();
        RollbackStage rollbackStage = releaseDescriptor.getCurrentRollbackStage();
        if((releaseStage == ReleaseStage.RELEASE_SYNC && isDone(job, releaseDescriptor)) || (rollbackStage == RollbackStage.ROLLBACK_MVN && job.isDone())) {
            releaseService.deleteLock();
            return true;
        }
        return false;
    }

    private boolean isDone(BatchJob<Repository, String> job, ReleaseDescriptor releaseDescriptor) {
        Repository repo = releaseDescriptor.getLastSuccessReleasedRepository();
        List<Project> projects = releaseDescriptor.getProjectList();
        return job.isDone() && repo != null && repo.equals(projects.get(projects.size() - 1).getRepository());
    }

    @GET
    @Path("/get-status-bar")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReleaseStatusMsg> getStatusBar(){
        ReleaseDescriptor releaseDescriptor = releaseService.getReleaseDescriptor();
        ReleaseStage releaseStage = releaseDescriptor.getCurrentReleaseStage();
        RollbackStage rollbackStage = releaseDescriptor.getCurrentRollbackStage();
        List<ReleaseStatusMsg> releaseStatusMsgs = new ArrayList<>();
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        if(releaseStage == ReleaseStage.NO_RELEASE) {
            for(RollbackStage rollStage : RollbackStage.values()) {
                if(rollStage == RollbackStage.NO_ROLLBACK) {
                    continue;
                }
                ReleaseStatusMsg releaseStatusMsg = new ReleaseStatusMsg();
                if(rollStage.ordinal() < rollbackStage.ordinal()) {
                    releaseStatusMsg.setMsg("Done");
                } else if(rollStage.ordinal() > rollbackStage.ordinal()) {
                    releaseStatusMsg.setMsg("Wait");
                } else {
                    if(job != null && job.isDone()) {
                        releaseStatusMsg.setMsg("Done");
                    } else {
                        releaseStatusMsg.setMsg("Process");
                    }
                }
                releaseStatusMsg.setName(rollStage.getText());
                releaseStatusMsgs.add(releaseStatusMsg);
            }
        } else {
            for(ReleaseStage relStage : ReleaseStage.values()) {
                if(relStage == ReleaseStage.NO_RELEASE) {
                    continue;
                }
                ReleaseStatusMsg releaseStatusMsg = new ReleaseStatusMsg();
                if(relStage.ordinal() < releaseStage.ordinal()) {
                    releaseStatusMsg.setMsg("Done");
                } else if(relStage.ordinal() > releaseStage.ordinal()) {
                    releaseStatusMsg.setMsg("Wait");
                } else {
                    if(job != null && job.isDone()) {
                        releaseStatusMsg.setMsg("Done");
                    } else {
                        releaseStatusMsg.setMsg("Process");
                    }
                }
                releaseStatusMsg.setName(relStage.getText());
                releaseStatusMsgs.add(releaseStatusMsg);
            }
        }

        return releaseStatusMsgs;
    }
}
