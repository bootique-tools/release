package io.bootique.tools.release.model.job;

public record JobStatusResponse(String projectName, String status, Double progress, String version, String branchName,
                                String result) {
}
