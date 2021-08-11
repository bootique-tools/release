package io.bootique.tools.release.service.release.stage.updater;

public interface StageListener {
    void addListener(Runnable stageListener);

    void removeListener();
}
