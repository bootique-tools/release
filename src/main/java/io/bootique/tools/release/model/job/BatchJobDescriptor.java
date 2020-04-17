package io.bootique.tools.release.model.job;

import java.util.Collection;
import java.util.function.Function;

public class BatchJobDescriptor<T, R> {

    private Collection<T> data;
    private Function<T, R> processor;
    private ErrorPolicy errorPolicy;
    private String controllerName;

    public static Builder builder(){
        return new Builder().errorPolicy(ErrorPolicy.SKIP_ON_ERROR);
    }

    public static class Builder {

        private BatchJobDescriptor descriptor;

        protected Builder() {
            this.descriptor = new BatchJobDescriptor();
        }

        public Builder data(Collection data) {
            this.descriptor.data = data;
            return this;
        }

        public Builder processor(Function processor) {
            this.descriptor.processor = processor;
            return this;
        }

        public Builder errorPolicy(ErrorPolicy errorPolicy) {
            this.descriptor.errorPolicy = errorPolicy;
            return this;
        }

        public Builder controllerName(String url) {
            this.descriptor.controllerName = url;
            return this;
        }

        public BatchJobDescriptor build(){
            return descriptor;
        }
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


}
