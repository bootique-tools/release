package io.bootique.tools.release.model.release;

public enum ReleaseStage {
    NO_RELEASE("No release"),
    RELEASE_PULL("Release pull"),
    RELEASE_VALIDATION("Release validation"),
    RELEASE_PREPARE("Release prepare"),
    RELEASE_PERFORM("Release perform"),
    RELEASE_SYNC("Release sync");

    private final String text;

    ReleaseStage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
