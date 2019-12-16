package io.bootique.tools.release.service.central;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.jersey.client.HttpTargets;
import io.bootique.tools.release.model.maven.Project;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public class DefaultMvnCentralService implements MvnCentralService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultMvnCentralService.class);

    @Inject
    HttpTargets targets;

    @Override
    public boolean isSync(String version, List<Project> projects) {
        for(Project project : projects) {
            Response response = targets
                    .newTarget("mvncentral")
                    .path("/solrsearch/select")
                    .queryParam("q", "g:%22"
                            + project.getRootModule().getGroup()
                            + "%22 AND a:%22"
                            + project.getRootModule().getId()
                            + "%22 AND v:%22"
                            + version
                            + "%22")
                    .queryParam("rows", "20")
                    .queryParam("wt", "json")
                    .request()
                    .buildGet()
                    .invoke();

            if(response.getStatus() != 200) {
                return true;
            }
            try {
                JsonNode node = new ObjectMapper().readTree(response.readEntity(String.class));
                if (node.get("response").get("docs").size() != 0) {
                    return true;
                }
            } catch (IOException e) {
                LOGGER.debug("Can't check syncing with maven central.");
                return true;
            }
        }

        return false;
    }
}
