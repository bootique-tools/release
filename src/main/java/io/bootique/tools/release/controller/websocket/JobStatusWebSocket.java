package io.bootique.tools.release.controller.websocket;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.service.job.BatchJobService;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ServerEndpoint(value = "/job/status")
public class JobStatusWebSocket {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(JobStatusWebSocket.class);
    private Session session;
    private BatchJob<?, ?> job;
    private Runnable listener;

    @Inject
    private BatchJobService batchJobService;

    @OnOpen
    public void open(Session session) {
        this.session = session;
        session.setMaxIdleTimeout(Integer.MAX_VALUE);

        // the listener binds to whichever job is current at connect time; with no job there is nothing to watch,
        // and a client that starts one is expected to reconnect
        job = batchJobService.getCurrentJob();
        if (job == null) {
            LOGGER.info("Job status WebSocket connected with no job running");
            return;
        }

        listener = () -> {
            if (!session.isOpen()) {
                return;
            }
            try {
                session.getBasicRemote().sendText("");
            } catch (IOException | RuntimeException e) {
                LOGGER.info("Unable to send job status to client", e);
            }
        };
        job.addListener(listener);
    }

    @OnClose
    public void close(Session session) {
        LOGGER.info("Job status WebSocket " + session.getId() + " close");
        unregister();
    }

    @OnError
    public void onError(Throwable error) throws IOException {
        LOGGER.error(error.getMessage(), error);
        try {
            session.close();
        } finally {
            unregister();
        }
    }

    private void unregister() {
        if (job != null && listener != null) {
            job.removeListener(listener);
        }
        job = null;
        listener = null;
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.debug("SEND MESSAGE: " + message);
    }
}
