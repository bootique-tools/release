package io.bootique.tools.release.service.preferences.credential;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.tools.release.service.bintray.BintrayApi;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.DefaultPreferenceService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.io.File;
import java.nio.file.Paths;

@BQConfig
public class PreferenceCredentialFactory {

    private String organizationName;
    private String organizationGroupId;
    private String gitHubToken;
    private String basePath;
    private String bintrayOrganizationName;

    private String logsPath;
    private String savePath;

    @BQConfigProperty("Organization name")
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    @BQConfigProperty("Organization group id")
    public void setOrganizationGroupId(String organizationGroupId) {
        this.organizationGroupId = organizationGroupId;
    }

    @BQConfigProperty("Github token")
    public void setGitHubToken(String gitHubToken) {
        this.gitHubToken = gitHubToken;
    }

    @BQConfigProperty
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @BQConfigProperty
    public void setBintrayOrganizationName(String bintrayOrganizationName) {
        this.bintrayOrganizationName = bintrayOrganizationName;
    }

    @BQConfigProperty
    public void setLogsPath(String logsPath) {
        this.logsPath = logsPath;
    }

    @BQConfigProperty
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public PreferenceService createPreferenceService(){
        if(gitHubToken == null) {
            throw new DesktopException("Can't find gitHub token.");
        }
        if(basePath == null) {
            throw new DesktopException("Can't find base path.");
        }

        PreferenceService preferences = new DefaultPreferenceService();

        if(!preferences.have(GitHubApi.ORGANIZATION_PREFERENCE)) {
            preferences.set(GitHubApi.ORGANIZATION_PREFERENCE, organizationName); // bootique
        }

        if(!preferences.have(MavenService.ORGANIZATION_GROUP_ID)) {
            preferences.set(MavenService.ORGANIZATION_GROUP_ID, organizationGroupId); // io.bootique
        }

        if(!preferences.have(GitHubApi.AUTH_TOKEN_PREFERENCE)) {
            preferences.set(GitHubApi.AUTH_TOKEN_PREFERENCE, gitHubToken);
        }

        if(!preferences.have(GitService.BASE_PATH_PREFERENCE)) {
            preferences.set(GitService.BASE_PATH_PREFERENCE, Paths.get(basePath));
        }

        if(!preferences.have(BintrayApi.BINTRAY_ORG_NAME)) {
            preferences.set(BintrayApi.BINTRAY_ORG_NAME, bintrayOrganizationName);
        }

        if (!preferences.have(ReleaseService.SAVE_PATH)) {
            preferences.set(ReleaseService.SAVE_PATH, savePath == null ? "service" + File.separator + "persist" : savePath);
        }

        if(!preferences.have(LoggerService.LOGGER_BASE_PATH)) {
            preferences.set(LoggerService.LOGGER_BASE_PATH, logsPath == null ? "service" + File.separator + "logs" : logsPath);
        }

        return preferences;
    }
}
