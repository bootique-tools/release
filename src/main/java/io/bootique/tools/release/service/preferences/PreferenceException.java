package io.bootique.tools.release.service.preferences;

public class PreferenceException extends RuntimeException {

    public PreferenceException(String message) {
        super(message);
    }

    public PreferenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
