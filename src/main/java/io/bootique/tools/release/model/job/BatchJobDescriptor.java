package io.bootique.tools.release.model.job;

import java.util.Collection;
import java.util.function.Function;

public class BatchJobDescriptor<T, R> {

    private Collection<T> data;
    private Function<T, R> processor;
    private ErrorPolicy errorPolicy;
    private String controllerName;

    public static <T,R> Builder<T,R> builder(){
        return new Builder<T,R>().errorPolicy(ErrorPolicy.SKIP_ON_ERROR);
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

    public String getControllerName() {
        return controllerName;
    }

    public static class Builder<T, R> {

        private final BatchJobDescriptor<T, R> descriptor;

        protected Builder() {
            this.descriptor = new BatchJobDescriptor<>();
        }

        public Builder<T, R> data(Collection<T> data) {
            this.descriptor.data = data;
            return this;
        }

        public Builder<T, R> processor(Function<T,R> processor) {
            this.descriptor.processor = processor;
            return this;
        }

        public Builder<T, R> errorPolicy(ErrorPolicy errorPolicy) {
            this.descriptor.errorPolicy = errorPolicy;
            return this;
        }

        public Builder<T, R> controllerName(String url) {
            this.descriptor.controllerName = url;
            return this;
        }

        public BatchJobDescriptor<T, R> build(){
            return descriptor;
        }
    }
}
