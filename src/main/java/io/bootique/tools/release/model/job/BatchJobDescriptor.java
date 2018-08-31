package io.bootique.tools.release.model.job;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class BatchJobDescriptor<T, R> {

    private final Collection<T> data;
    private final Function<T, R> processor;
    private final ErrorPolicy errorPolicy;

    public BatchJobDescriptor(Collection<T> data, Function<T, R> processor, ErrorPolicy errorPolicy) {
        this.data = Objects.requireNonNull(data);
        this.processor = Objects.requireNonNull(processor);
        this.errorPolicy = Objects.requireNonNull(errorPolicy);
    }

    public BatchJobDescriptor(Collection<T> data, Function<T, R> processor) {
        this(data, processor, ErrorPolicy.SKIP_ON_ERROR);
    }

    public Collection<T> getData() {
        return data;
    }

    /**
     * Data processing function,
     * can throw {@link io.bootique.tools.release.service.job.JobException} in case of error.
     *
     * @return function that should be applied to all data
     */
    public Function<T, R> getProcessor() {
        return processor;
    }

    public ErrorPolicy getErrorPolicy() {
        return errorPolicy;
    }
}
