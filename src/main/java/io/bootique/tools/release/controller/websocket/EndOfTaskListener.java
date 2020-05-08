package io.bootique.tools.release.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;

public class EndOfTaskListener implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndOfTaskListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Session session;
    private final BatchJobService jobService;

    public EndOfTaskListener(Session session, BatchJobService jobService) {
        this.session = session;
        this.jobService = jobService;
    }

    public void run() {
        if (!session.isOpen()) {
            return;
        }

        BatchJob<Repository, String> job = jobService.getCurrentJob();
        try {
            JobResponse response = JobResponse.builder()
                    .name(job.getBatchJobDescriptor().getControllerName())
                    .percent(job.getProgress())
                    .build();
            this.session.getBasicRemote().sendObject(MAPPER.writeValueAsString(response));
            if (job.isDone()) {
                session.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
