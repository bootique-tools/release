package io.bootique.tools.release.controller.websocket;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StageStatusListener implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StageStatusListener.class);

    private enum Commands {
        LOAD
    }

    private final Session session;

    public StageStatusListener(Session session) {
        this.session = session;
    }

    @Override
    public void run() {
        if (session.isOpen()) {
            try {
                this.session.getBasicRemote().sendText(Commands.LOAD.toString());
            } catch (IOException e) {
                logger.info("Unable to send message to client", e);
            }
        }
    }
}


