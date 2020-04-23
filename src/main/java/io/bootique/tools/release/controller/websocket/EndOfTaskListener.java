package io.bootique.tools.release.controller.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobResponse;
import io.bootique.value.Percent;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

public class EndOfTaskListener {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(EndOfTaskListener.class);
    private final Session session;
    private final JobResponse.Builder builder = JobResponse.builder();
    private final BatchJobService jobService;

    public EndOfTaskListener(Session session, BatchJobService jobService) {
        this.session = session;
        this.jobService = jobService;
    }

    public void update() {
        BatchJob<Repository, String> job = jobService.getCurrentJob();
        boolean flag = false;

        try {
            if (session.isOpen()) {
                if (job.getTotal() != job.getDone() + 1) {
                    builder.percent(job.getProgress()).name(job.getBatchJobDescriptor().getControllerName());
                } else {
                    builder.percent(new Percent("100")).name(job.getBatchJobDescriptor().getControllerName());
                    flag = true;
                }
                this.session.getBasicRemote().sendObject(mapper.writeValueAsString(builder.build()));
            } else {
                throw new IOException("session closed");
            }
            if (flag) {
                session.close();
            }
        } catch (IOException | EncodeException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
