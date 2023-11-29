package io.bootique.tools.release.controller.websocket;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.service.job.BatchJobService;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/job/status")
public class JobStatusWebSocket {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(JobStatusWebSocket.class);
    private Session session;

    @Inject
    private BatchJobService batchJobService;

    @OnOpen
    public void open(Session session) throws IOException {
        this.session = session;
        session.setMaxIdleTimeout(Integer.MAX_VALUE);
        batchJobService.getCurrentJob().addListener(()->{
            try {
                session.getBasicRemote().sendText("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @OnClose
    public void close(Session session) {
        LOGGER.info("Job status WebSocket " + session.getId() + " close");
    }

    @OnError
    public void onError(Throwable error) throws IOException {
        LOGGER.error(error.getMessage(), error);
        session.close();
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.debug("SEND MESSAGE: " + message);
    }
}
