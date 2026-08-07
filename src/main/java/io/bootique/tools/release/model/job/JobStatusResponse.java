package io.bootique.tools.release.model.job;

/**
 * Progress of the current batch job. Every field except {@code jobRunning} is null until the first
 * task completes, so {@code jobRunning} is the only way to tell a freshly submitted job from no job
 * at all.
 */
public record JobStatusResponse(boolean jobRunning, String projectName, String status, Double progress,
                                String version, String branchName, String result) {

    public static final JobStatusResponse NO_JOB =
            new JobStatusResponse(false, null, null, 0.0, null, null, null);

    /** A job exists, but no task has finished yet. */
    public static JobStatusResponse started() {
        return new JobStatusResponse(true, null, null, 0.0, null, null, null);
    }
}
