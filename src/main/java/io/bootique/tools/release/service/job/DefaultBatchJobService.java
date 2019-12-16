package io.bootique.tools.release.service.job;

import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.tools.release.model.job.BatchJobStatus;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.value.Percent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class DefaultBatchJobService implements BatchJobService {

    @Inject
    private PreferenceService preferences;

    private final static AtomicLong JOB_ID_SEQUENCE = new AtomicLong();

    private final Map<Long, BatchJob<?, ?>> jobMap;
    private volatile ForkJoinPool pool;
    private volatile long lastCleanupAtId;

    public DefaultBatchJobService() {
        jobMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T, R> BatchJob<T, R> submit(BatchJobDescriptor<T, R> descriptor) {
        cleanUp();

        List<BatchForkJoinTask<BatchJobResult<T, R>>> tasks = descriptor.getData().stream()
                .map(data -> pool.submit((ForkJoinTask<BatchJobResult<T, R>>) new BatchForkJoinTask<>(
                        new BatchJobResultCallable<>(descriptor, data),
                        new BatchJobResult<>(BatchJobStatus.IN_PROGRESS, data, (R)null)))
                )
                .map(task -> (BatchForkJoinTask<BatchJobResult<T, R>>)task)
                .collect(Collectors.toList());

        BatchJob<T, R> job = new BatchJob<>(JOB_ID_SEQUENCE.getAndIncrement(), tasks);
        jobMap.put(job.getId(), job);
        return job;
    }

    private <R> R getResultFromException(Exception ex) {
        if(ex instanceof JobException) {
            return ((JobException) ex).getResult();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R> BatchJob<T, R> getJobById(long id) {
        return (BatchJob<T, R>)jobMap.get(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R> BatchJob<T, R> getCurrentJob() {
        return preferences.have(BatchJobService.CURRENT_JOB_ID) ? getJobById(preferences.get(BatchJobService.CURRENT_JOB_ID)) : null;
    }

    private void cleanUp() {
        if(lastCleanupAtId != JOB_ID_SEQUENCE.get()) {
            lastCleanupAtId = JOB_ID_SEQUENCE.get();
            jobMap.entrySet().removeIf(entry -> entry.getValue().getProgress().equals(Percent.HUNDRED));
        }

        synchronized(this) {
            if (pool == null || pool.isShutdown()) {
                pool = new ForkJoinPool(1);
            }
        }
    }

    private class BatchJobResultCallable<T, R> implements Callable<BatchJobResult<T, R>> {
        private final BatchJobDescriptor<T, R> descriptor;
        private final T data;

        private BatchJobResultCallable(BatchJobDescriptor<T, R> descriptor, T data) {
            this.descriptor = descriptor;
            this.data = data;
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
                        pool.shutdownNow(); // TODO: is there a better way?
                        break;
                    case RETRY_ON_ERROR:
                        try {
                            return execute();
                        } catch(Exception ex2) {
                            return new BatchJobResult<>(BatchJobStatus.FAILURE, data, DefaultBatchJobService.this.getResultFromException(ex2));
                        }
                }
                return new BatchJobResult<>(BatchJobStatus.FAILURE, data, DefaultBatchJobService.this.getResultFromException(ex));
            }
        }

        private BatchJobResult<T, R> execute() {
            R result = descriptor.getProcessor().apply(data);
            return new BatchJobResult<>(BatchJobStatus.SUCCESS, data, result);
        }
    }
}
