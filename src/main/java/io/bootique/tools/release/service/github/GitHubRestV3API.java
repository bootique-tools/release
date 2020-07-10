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
    public Milestone createMilestone(Repository repository, String title, String description) throws IOException {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);

        Response response = prepareRequest(
                "/repos/" + repository.getOrganization().getLogin() + "/" + repository.getName() + "/milestones")
                .buildPost(Entity.json(mapper.writeValueAsString(data)))
                .invoke();

        if (response.getStatus() != 201) {
            throw new DesktopException("Can't create milestone for " + repository.getName());
        }
        return jsonToMilestone(repository, mapper.readTree(response.readEntity(String.class)));
    }

    private void patchMilestone(Repository repository, String title, String description, String state, int id) {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("state", state);
        data.put("description", description);

        if (state.equals("closed")) {
            repository.getMilestones().forEach(milestone -> {
                if (milestone.getTitle().equals(title)) {
                    milestone.setState("CLOSED");
                }
            });
        }

        Response response;
        try {
            response = prepareRequest(
                    "/repos/" + repository.getOrganization().getLogin() + "/" + repository.getName() + "/milestones/" + id)
                    .build("PATCH", Entity.json(mapper.writeValueAsString(data)))
                    .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .invoke();
        } catch (JsonProcessingException e) {
            throw new DesktopException(e.getMessage());
        }

        if (response.getStatus() != 200) {
            throw new DesktopException("Can't patch milestone for " + repository.getName());
        }
    }

    @Override
    public void renameMilestone(Repository repository, String title, String description, String newTitle) {
        patchMilestone(repository, newTitle, description, "open", repository.getMilestoneId(title));
    }

    @Override
    public void closeMilestone(Repository repository, String title, String description) {
        patchMilestone(repository, title, description, "closed", repository.getMilestoneId(title));
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

    private Milestone jsonToMilestone(Repository repository, JsonNode json) {
        Milestone milestone = new Milestone();
        milestone.setRepository(repository);
        milestone.setNumber(json.get("number").asInt());
        milestone.setTitle(json.get("title").asText());
        milestone.setUrl(json.get("html_url").asText());
        milestone.setState(json.get("state").asText());
        milestone.setObjectId(new ObjectId("Milestone", json.get("id").asText().getBytes()));
        return milestone;
    }

}
