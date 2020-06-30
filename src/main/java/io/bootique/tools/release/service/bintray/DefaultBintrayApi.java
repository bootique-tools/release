package io.bootique.tools.release.service.bintray;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class DefaultBintrayApi implements BintrayApi {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultBintrayApi.class);

    HttpTargets targets;
    private ObjectMapper objectMapper;
    PreferenceService preferenceService;

    private String username;
    private String password;
    private int close;

    DefaultBintrayApi() {
    }

    public DefaultBintrayApi(HttpTargets httpTargets, ObjectMapper objectMapper, PreferenceService preferenceService, String username, String password, int close) {
        this.targets = httpTargets;
        this.objectMapper = objectMapper;
        this.preferenceService = preferenceService;
        this.username = username;
        this.password = password;
        this.close = close;
    }

    @Override
    public void publishUploadedContent(Repository repository, String releaseVersion) {
        LOGGER.debug("Publish content of " + repository.getName() + " in Bintray.");

        System.out.println("\nxpublishUploadedContent\n");

        Response response = buildPost("/content/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases/" + repository.getName() + "/" + releaseVersion + "/publish", "");
        if (response.getStatus() != 200) {
            throw new DesktopException("Exit code: " + response.getStatus() + "\n" + response.readEntity(String.class));
        }
        LOGGER.debug("Content was published. Response is " + response.getStatus() + '\n'
                + "Message: " + response.readEntity(String.class));
    }

    @Override
    public void syncWithCentral(Repository repository, String releaseVersion) {
        LOGGER.debug("Start syncing with maven central " + repository.getName() + " in Bintray.");
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        map.put("close", close);

        Response response;
        try {
            response = buildPost("/maven_central_sync/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases/" + repository.getName() + "/versions/" + releaseVersion,
                    objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new DesktopException("Can't process response.", e);
        }

        if (response.getStatus() != 200) {
            throw new DesktopException("Exit code: " + response.getStatus() + "\n" + response.readEntity(String.class));
        }
        LOGGER.debug("Synced with maven central. Response is " + response.getStatus() + '\n'
                + "Message: " + response.readEntity(String.class));
    }

    @Override
    public boolean getRepository(Repository repository) {
        LOGGER.debug("Check repository:" + repository.getName() + " in Bintray.");
        Response response = buildGet("/packages/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases/" + repository.getName());
        LOGGER.debug("Check repository... Response is " + response.getStatus() + '\n'
                + "Message: " + response.readEntity(String.class));
        if (response.getStatus() != 200) {
            LOGGER.warn(repository.getName() + " hasn't got Bintray repo.");
            return false;
        }
        return true;
    }

    @Override
    public void createRepository(Repository repository) {
        LOGGER.debug("Start create repository: " + repository.getName() + " in Bintray.");
        Map<String, Object> map = new HashMap<>();
        map.put("name", repository.getName());
        map.put("desc", repository.getDescription());
        map.put("licenses", new String[]{"Apache-2.0"});
        map.put("vcs_url", repository.getUrl());
        map.put("website_url", "https://bootique.io");
        map.put("issue_tracker_url", repository.getUrl() + "/issues");
        map.put("github_repo", repository.getOrganization().getLogin() + "/" + repository.getName());
        map.put("github_release_notes_file", "RELEASE-NOTES.md");

        Response response;
        try {
            response = buildPost("/packages/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases", objectMapper.writeValueAsString(map));
            if (response.getStatus() != 201) {
                throw new DesktopException("Exit code: " + response.getStatus() + "\n" + response.readEntity(String.class));
            }
        } catch (JsonProcessingException e) {
            throw new DesktopException("Can't process response.", e);
        }

        LOGGER.debug("Created repository... Response is " + response.getStatus() + '\n'
                + "Message: " + response.readEntity(String.class));
    }

    @Override
    public void getAndDeleteVersion(Repository repository, String releaseVersion) {
        LOGGER.debug("Get repository " + repository.getName() + " from Bintray.");
        Response response = buildGet("/packages/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases/" + repository.getName() + "/versions/" + releaseVersion);
        LOGGER.debug("Getting repository... Response is " + response.getStatus() + '\n'
                + "Message: " + response.readEntity(String.class));
        if (response.getStatus() == 200) {
            deleteVersion(repository, releaseVersion);
        }
    }

    private void deleteVersion(Repository repository, String releaseVersion) {
        LOGGER.debug("Delete repository " + repository.getName() + " from Bintray.");
        Response response = buildDelete("/packages/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases/" + repository.getName() + "/versions/" + releaseVersion);
        if (response.getStatus() != 200) {
            throw new DesktopException("Exit code: " + response.getStatus() + "\n" + response.readEntity(String.class));
        }
        LOGGER.debug("Deleted repository... Response is " + response.getStatus() + '\n'
                + "Message: " + response.readEntity(String.class));
    }

    private Response buildPost(String path, String json) {
        return targets
                .newTarget("bintray")
                .path(path)
                .request()
                .buildPost(Entity.json(json))
                .invoke();
    }

    private Response buildGet(String path) {
        return targets
                .newTarget("bintray")
                .path(path)
                .request()
                .buildGet()
                .invoke();
    }

    private Response buildDelete(String path) {
        return targets
                .newTarget("bintray")
                .path(path)
                .request()
                .buildDelete()
                .invoke();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getPackageInfo(Project project) {
        Response response = targets.newTarget("bintray")
                .path("/packages/" + preferenceService.get(BintrayApi.BINTRAY_ORG_NAME) + "/releases/" + project.getRootModule().getId())
                .request().buildGet().invoke();
        return (Map<String, String>) response.readEntity(HashMap.class);
    }
}
