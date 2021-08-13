package io.bootique.tools.release.controller.websocket;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.service.release.stage.updater.StageListener;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "ui/release/socket")
public class ReleaseWebSocket {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ReleaseWebSocket.class);
    private Session session;

    @Inject
    public StageListener stageUpdaterListener;

    @OnOpen
    public void open(Session session) throws IOException {
        this.session = session;
        session.setMaxIdleTimeout(Integer.MAX_VALUE);
        LOGGER.info("WebSocket connect");

        stageUpdaterListener.addListener(new StageStatusListener(session));
    }

    @OnClose
    public void close(Session session) {
        LOGGER.info("WebSocket " + session.getId() + " close");
    }

    @OnError
    public void onError(Throwable error) throws IOException {
        LOGGER.error(error.getMessage(), error);
        session.close();
        stageUpdaterListener.removeListener();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        LOGGER.error("SEND MESSAGE: " + message);
    }
}
