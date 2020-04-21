package io.bootique.tools.release.controller.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.JobResponse;
import io.bootique.value.Percent;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/job-progress-web-socket")
public class JobProgressWebSocket implements CallbackListener {

    @Inject
    private BatchJobService jobService;

    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(JobProgressWebSocket.class);
    private Session session;

    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
        jobService.getCurrentJob().addListener(this);
        LOGGER.info("WebSocket connect");
    }

    @OnError
    public void onError(Throwable t) throws IOException {
        session.close();
        LOGGER.error(t.getMessage());
    }

    @OnClose
    public void onClose() {
        LOGGER.info("WebSocket close");
    }

    @Override
    public void updateProgress() throws EncodeException, IOException {

        boolean flag = false;
        JobResponse.Builder builder = JobResponse.builder();
        BatchJob<Repository, String> job = jobService.getCurrentJob();

        if (job.getTotal() != job.getDone() + 1) {
            builder.percent(job.getProgress()).name(job.getBatchJobDescriptor().getControllerName());
        } else {
            builder.percent(new Percent("100")).name(job.getBatchJobDescriptor().getControllerName());
            flag = true;
        }
        this.session.getBasicRemote().sendObject(mapper.writeValueAsString(builder.build()));

        if (flag) {
            session.close();
        }
    }
}
