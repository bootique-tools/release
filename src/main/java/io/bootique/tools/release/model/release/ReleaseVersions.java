package io.bootique.tools.release.model.release;

public class ReleaseVersions {

    private String releaseVersion;
    private String devVersion;
    private String fromVersion;

    public ReleaseVersions(String releaseVersion, String devVersion, String fromVersion) {
        this.releaseVersion = releaseVersion;
        this.devVersion = devVersion;
        this.fromVersion = fromVersion;
    }

    public ReleaseVersions() {
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getDevVersion() {
        return devVersion;
    }

    public String getFromVersion() {
        return fromVersion;
    }
}
