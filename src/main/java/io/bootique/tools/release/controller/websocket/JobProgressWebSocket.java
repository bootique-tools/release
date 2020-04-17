package io.bootique.tools.release.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobResponse;
import io.bootique.value.Percent;

import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/job-progress-web-socket")
public class JobProgressWebSocket {

    @Inject
    private BatchJobService jobService;

    private ObjectMapper mapper = new ObjectMapper();

    private Session session;

    private void checkProgress() throws IOException, EncodeException {
        JobResponse.Builder builder = JobResponse.builder();

        int jobDoneCount = 0;
        boolean flag = false;

        do {
            BatchJob<Repository, String> job = jobService.getCurrentJob();
            if (job == null) {
                builder.percent(new Percent("0"));
            } else {
                builder.percent(job.getProgress()).name(job.getBatchJobDescriptor().getControllerName());
                if (job.isDone()) {
                    builder.name(job.getBatchJobDescriptor().getControllerName())
                            .percent(new Percent("100"));
                    flag = true;
                    jobDoneCount = 0;
                }
            }
            if (job != null && jobDoneCount != job.getDone()) {
                sendProgress(mapper.writeValueAsString(builder.build()));
                jobDoneCount = job.getDone();
            }
            if (flag) {
                session.close();
            }
        } while (!flag);
    }

    @OnOpen
    public void onConnect(Session session) throws IOException, EncodeException {
        this.session = session;
        checkProgress();
    }

    private void sendProgress(String progress) throws IOException, EncodeException {
        this.session.getBasicRemote().sendObject(progress);
    }
}
