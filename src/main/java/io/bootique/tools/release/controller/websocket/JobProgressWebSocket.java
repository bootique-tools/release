package io.bootique.tools.release.controller.websocket;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.service.job.BatchJobService;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/job-progress-web-socket")
public class JobProgressWebSocket {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(JobProgressWebSocket.class);
    private Session session;

    @Inject
    private BatchJobService jobService;

    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
        jobService.getCurrentJob().addListener(new EndOfTaskListener(session, jobService));
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
}
