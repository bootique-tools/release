package io.bootique.tools.release.service.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.apache.cayenne.ObjectId;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GitHubRestV3API implements GitHubRestAPI {

    @Inject
    private PreferenceService preferences;

    @Inject
    private HttpTargets targets;

    private final ObjectMapper mapper;

    public GitHubRestV3API() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());
    }

    /**
     * {
     * "title":         "v1.0",                                 (required)
     * "state":         "open",                                 (optional)
     * "description":   "Tracking milestone for version 1.0",   (optional)
     * "due_on":        "2012-10-09T23:39:01Z"                  (optional)
     * }
     */
    @Override
    public Milestone createMilestone(Repository repository, String title) {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("description", "");

        try {
            Response response = prepareRequest(
                    "/repos/" + repository.getOrganization().getLogin() + "/" + repository.getName() + "/milestones")
                    .buildPost(Entity.json(mapper.writeValueAsString(data)))
                    .invoke();

            if (response.getStatus() != 201) {
                throw new DesktopException("Can't create milestone for " + repository.getName());
            }
            return createMilestone(repository, mapper.readTree(response.readEntity(String.class)));
        } catch (IOException ex) {
            throw new DesktopException("Can't create milestone for " + repository.getName(), ex);
        }
    }

    @Override
    public void renameMilestone(Milestone milestone, String newTitle) {
        patchMilestone(milestone, newTitle, "open");
    }

    @Override
    public void closeMilestone(Milestone milestone) {
        patchMilestone(milestone, milestone.getTitle(), "closed");
    }

    private void patchMilestone(Milestone milestone, String title, String state) {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("state", state);

        Repository repository = milestone.getRepository();
        Response response;
        try {
            response = prepareRequest(
                    "/repos/" + repository.getOrganization().getLogin() + "/" + repository.getName() + "/milestones/" + milestone.getNumber())
                    .build("PATCH", Entity.json(mapper.writeValueAsString(data)))
                    .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .invoke();
        } catch (JsonProcessingException e) {
            throw new DesktopException(e.getMessage());
        }

        if (response.getStatus() != 200) {
            throw new DesktopException("Can't patch milestone for " + repository.getName());
        }

        milestone.setTitle(title);
        milestone.setState(state);
    }

    private Invocation.Builder prepareRequest(String path) {
        String token = preferences.get(GitHubApiImport.AUTH_TOKEN_PREFERENCE);
        return targets
                .newTarget("github")
                .path(path)
                .request()
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token " + token);
    }

    private Milestone createMilestone(Repository repository, JsonNode json) {
        Milestone milestone = new Milestone();
        milestone.setNumber(json.get("number").asInt());
        milestone.setTitle(json.get("title").asText());
        milestone.setUrl(json.get("html_url").asText());
        milestone.setState(json.get("state").asText());
        milestone.setObjectId(ObjectId.of("Milestone"));
        milestone.setRepository(repository);
        return milestone;
    }

}
