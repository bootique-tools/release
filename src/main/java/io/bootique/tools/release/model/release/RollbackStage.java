package io.bootique.tools.release.model.release;

public enum RollbackStage {
    NO_ROLLBACK("No rollback"),
    ROLLBACK_SONATYPE("Rollback sonatype"),
    ROLLBACK_MVN("Rollback git");

    private final String text;

    RollbackStage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
