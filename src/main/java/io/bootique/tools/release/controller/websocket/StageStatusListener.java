package io.bootique.tools.release.controller.websocket;

import javax.websocket.Session;
import java.io.IOException;

public class StageStatusListener implements Runnable {

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
                e.printStackTrace();
            }
        }
    }
}


