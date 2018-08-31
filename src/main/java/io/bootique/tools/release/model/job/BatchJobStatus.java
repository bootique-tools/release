package io.bootique.tools.release.model.job;

public enum BatchJobStatus {
    IN_PROGRESS ("secondary"),
    FAILURE     ("danger"),
    SUCCESS     ("success");

    private final String style;

    BatchJobStatus(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }
}
