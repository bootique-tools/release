package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class ToolCheckView extends BaseView {

    private final String javaVersion;
    private final String jdk;
    private final String mavenVersion;

    public ToolCheckView(User user, Organization organization, String javaVersion, String jdk, String mavenVersion) {
        super("tool-check", user, organization);
        this.javaVersion = javaVersion;
        this.jdk = jdk;
        this.mavenVersion = mavenVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJdk() {
        return jdk;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }
}
