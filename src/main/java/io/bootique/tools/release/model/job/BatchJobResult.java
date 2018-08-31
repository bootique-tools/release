package io.bootique.tools.release.model.job;

public class BatchJobResult<T, R> {

    private final BatchJobStatus status;
    private final T data;
    private final R result;

    public BatchJobResult(BatchJobStatus status, T data, R result) {
        this.status = status;
        this.data = data;
        this.result = result;
    }

    public BatchJobStatus getStatus() {
        return status;
    }

    public R getResult() {
        return result;
    }

    public T getData() {
        return data;
    }
}
