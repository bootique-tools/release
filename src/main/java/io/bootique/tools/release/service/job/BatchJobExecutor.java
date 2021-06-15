package io.bootique.tools.release.service.job;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class BatchJobExecutor extends AbstractExecutorService {

    private ExecutorService threadPool;

    BatchJobExecutor() {
        this.threadPool = Executors.newSingleThreadExecutor();
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new BatchJobTask<>(callable);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Runnable command) {
        threadPool.execute(command);
    }

    @Override
    public void shutdown() {
        threadPool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return threadPool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPool.awaitTermination(timeout, unit);
    }

    public void checkState() {
        if (threadPool == null || threadPool.isShutdown()) {
            threadPool = Executors.newSingleThreadExecutor();
        }
    }
}
