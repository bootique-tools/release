package io.bootique.tools.release.service.job;

public class JobException extends RuntimeException {

    private final Object result;

    public JobException(Object result, String message) {
        super(message);
        this.result = result;
    }

    public JobException(Object result, Throwable cause) {
        super(cause.getMessage(), cause);
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    public <T> T getResult() {
        return (T)result;
    }
}
