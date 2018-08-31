package io.bootique.tools.release.service.job;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

public class BatchForkJoinTask<T> extends ForkJoinTask<T> implements RunnableFuture<T> {

    private final Callable<? extends T> callable;
    private volatile T result;

    BatchForkJoinTask(Callable<? extends T> callable, T initialState) {
        this.callable = Objects.requireNonNull(callable);
        this.result = initialState;
    }

    @Override
    public final T getRawResult() {
        return result;
    }

    public T getInitialState() {
        return result;
    }

    @Override
    protected final void setRawResult(T v) {
        result = v;
    }

    @Override
    public final boolean exec() {
        try {
            result = callable.call();
            return true;
        } catch (Error | RuntimeException err) {
            throw err;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final void run() {
        invoke();
    }

}
