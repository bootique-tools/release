package io.bootique.tools.release.service.release.stage.updater;

/**
 * Registry of callbacks notified on every release stage transition. Listeners are independent, so several
 * clients can watch the same release at once.
 */
public interface StageListener {

    void addListener(Runnable stageListener);

    void removeListener(Runnable stageListener);

}
