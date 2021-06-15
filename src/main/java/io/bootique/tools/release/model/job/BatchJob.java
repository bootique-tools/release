package io.bootique.tools.release.model.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bootique.tools.release.service.job.BatchJobTask;
import io.bootique.value.Percent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class BatchJob<T, R> {

    private final long id;
    private final long startTimeNanos;
    private final BatchJobDescriptor<T, R> batchJobDescriptor;

    @JsonIgnore
    private final List<BatchJobTask<BatchJobResult<T, R>>> tasks;

    @JsonIgnore
    private final Collection<Runnable> endOfTaskListeners = new ArrayList<>();

    @JsonIgnore
    private final Runnable taskEndListener = () -> endOfTaskListeners.forEach(Runnable::run);

    public BatchJob(long id, List<BatchJobTask<BatchJobResult<T, R>>> tasks, BatchJobDescriptor<T, R> batchJobDescriptor) {
        this.id = id;
        this.startTimeNanos = System.nanoTime();
        this.tasks = tasks;
        this.tasks.forEach(t -> t.setListener(taskEndListener));
        this.batchJobDescriptor = batchJobDescriptor;
    }

    public void addListener(Runnable taskEndListener) {
        this.endOfTaskListeners.add(taskEndListener);
    }

    public long getId() {
        return id;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    @JsonIgnore
    public Duration elapsedTime() {
        return Duration.ofNanos(System.nanoTime() - startTimeNanos);
    }

    public int getTotal() {
        return tasks.size();
    }

    public BatchJobDescriptor<T, R> getBatchJobDescriptor() {
        return batchJobDescriptor;
    }

    public int getDone() {
        return (int) tasks.stream().filter(FutureTask::isDone).count();
    }

    @JsonIgnore
    public Percent getProgress() {
        if(isDone()) {
            return Percent.HUNDRED;
        }
        if(getTotal() == 0) {
            return Percent.ZERO;
        }
        return new Percent((double) getDone() / getTotal());
    }

    @JsonIgnore
    public boolean isDone() {
        return getDone() == getTotal();
    }

    public List<BatchJobResult<T, R>> getResults() {
        return tasks.stream()
                .map(task -> {
                    try {
                        return task.isDone()
                                ? task.get()
                                : new BatchJobResult<T, R>(BatchJobStatus.IN_PROGRESS, task.getData(), null);
                    } catch (Exception ex) {
                        return new BatchJobResult<T, R>(BatchJobStatus.FAILURE, task.getData(), null);
                    }
                })
                .collect(Collectors.toList());
    }
}
