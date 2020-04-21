package io.bootique.tools.release.controller.websocket;

import javax.websocket.EncodeException;
import java.io.IOException;

public interface CallbackListener {

    void updateProgress() throws EncodeException, IOException;
}
