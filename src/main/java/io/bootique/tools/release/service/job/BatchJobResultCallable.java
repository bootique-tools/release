package io.bootique.tools.release.service.job;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.tools.release.model.job.BatchJobStatus;

class BatchJobResultCallable<T, R> implements Callable<BatchJobResult<T, R>> {
    private final ExecutorService executorService;
    private final BatchJobDescriptor<T, R> descriptor;
    private final T data;

    BatchJobResultCallable(ExecutorService executorService, BatchJobDescriptor<T, R> descriptor, T data) {
        this.executorService = executorService;
        this.descriptor = descriptor;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    @Override
    public BatchJobResult<T, R> call() {
        try {
            return execute();
        } catch (Exception ex) {
            switch (descriptor.getErrorPolicy()) {
                case SKIP_ON_ERROR:
                    break;
                case ABORT_ALL_ON_ERROR:
                    executorService.shutdownNow(); // TODO: is there a better way?
                    break;
                case RETRY_ON_ERROR:
                    try {
                        return execute();
                    } catch (Exception ex2) {
                        return new BatchJobResult<>(BatchJobStatus.FAILURE, data, getResultFromException(ex2));
                    }
            }
            return new BatchJobResult<>(BatchJobStatus.FAILURE, data, getResultFromException(ex));
        }
    }

    private BatchJobResult<T, R> execute() {
        R result = descriptor.getProcessor().apply(data);
        return new BatchJobResult<>(BatchJobStatus.SUCCESS, data, result);
    }

    private R getResultFromException(Exception ex) {
        if(ex instanceof JobException jex) {
            return jex.getResult();
        } else {
            return null;
        }
    }
}
