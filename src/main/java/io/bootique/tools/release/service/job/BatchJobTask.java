package io.bootique.tools.release.service.job;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class BatchJobTask<T> extends FutureTask<T> {

    private final Object data;
    private Runnable listener;

    public BatchJobTask(Callable<T> callable) {
        super(callable);
        if(callable instanceof BatchJobResultCallable jobResult) {
            data = jobResult.getData();
        } else {
            throw new RuntimeException("BatchJobResultCallable expected, got " + callable);
        }
    }

    public BatchJobTask(Runnable runnable, T value) {
        super(runnable, value);
        throw new RuntimeException("BatchJobResultCallable expected, got runnable");
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    protected void done() {
        listener.run();
    }

    @SuppressWarnings("unchecked")
    public <D> D getData() {
        return (D)data;
    }
}
