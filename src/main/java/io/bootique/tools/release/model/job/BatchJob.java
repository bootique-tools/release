package io.bootique.tools.release.model.job;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bootique.tools.release.controller.websocket.CallbackListener;
import io.bootique.tools.release.service.job.BatchForkJoinTask;
import io.bootique.value.Percent;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

public class BatchJob<T, R> {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(BatchJob.class);
    private final long id;
    private final long startTimeNanos;
    private BatchJobDescriptor<T, R> batchJobDescriptor;

    @JsonIgnore
    private List<BatchForkJoinTask<BatchJobResult<T, R>>> tasks;

    private CallbackListener endOfTaskListener;

    private Runnable listener = () -> {
        try {
            endOfTaskListener.updateProgress();
        } catch (EncodeException | IOException e) {
            LOGGER.error(e.getMessage());
        }
    };

    public BatchJob(long id, List<BatchForkJoinTask<BatchJobResult<T, R>>> tasks, BatchJobDescriptor<T, R> batchJobDescriptor) {
        this.id = id;
        this.startTimeNanos = System.nanoTime();
        this.tasks = tasks;
        this.tasks.forEach(t -> t.setListener(listener));
        this.batchJobDescriptor = batchJobDescriptor;
    }

    public void addListener(CallbackListener callbackListener) {
        this.endOfTaskListener = callbackListener;
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
        return (int) tasks.stream().filter(ForkJoinTask::isDone).count();
    }

    @JsonIgnore
    public Percent getProgress() {
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
                        return task.isDone() ? task.get() : task.getInitialState();
                    } catch (InterruptedException | ExecutionException | CancellationException ex) {
                        return task.getInitialState();
                    }
                })
                .collect(Collectors.toList());
    }
}
