package io.bootique.tools.release.service.desktop;

public class DesktopException extends RuntimeException {

    public DesktopException(String message) {
        super(message);
    }

    public DesktopException(String message, Throwable cause) {
        super(message, cause);
    }

}
