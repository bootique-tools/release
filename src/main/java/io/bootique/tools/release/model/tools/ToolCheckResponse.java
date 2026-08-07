package io.bootique.tools.release.model.tools;

/**
 * Versions of the external tools the release pipeline shells out to. Each field either holds the raw tool output
 * or an "Unable to run ..." message, so the UI can render it verbatim without branching on success.
 */
public record ToolCheckResponse(String javaVersion, String jdk, String mavenVersion) {
}
