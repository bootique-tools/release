package io.bootique.tools.release.model.release;

/**
 * Whether a release is currently in flight, and if so which versions it targets. The UI uses this to decide
 * between the project-selection page and the release board without having to probe a stage endpoint.
 */
public record ReleaseSummary(boolean hasDescriptor, String fromVersion, String releaseVersion, String devVersion) {

    public static final ReleaseSummary NONE = new ReleaseSummary(false, null, null, null);

    public static ReleaseSummary of(ReleaseVersions versions) {
        return new ReleaseSummary(true, versions.fromVersion(), versions.releaseVersion(), versions.devVersion());
    }
}
