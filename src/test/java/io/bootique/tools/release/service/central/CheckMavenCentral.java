package io.bootique.tools.release.service.central;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.maven.persistent.Module;
import io.bootique.tools.release.model.maven.persistent.Project;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckMavenCentral {

    private DefaultMvnCentralService mvnCentralService;

    private ObjectContext context;

    @BeforeEach
    void createService() {
        mvnCentralService = new DefaultMvnCentralService();
        mvnCentralService.targets = targets ->
                ClientBuilder.newClient().target("http://search.maven.org");

        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();
    }

    @Test
    @DisplayName("Maven central request test.")
    void testMavenCentralRequest() {
        Repository repository = context.newObject(Repository.class);
        repository.setName("bootique-job-parent");
        Module module = new Module("io.bootique.job", "bootique-job-parent", "0.25");
        assertTrue(mvnCentralService.isSync("0.25",
                Collections.singletonList(new Project(repository, Paths.get(""), module))));

        Repository repository1 = context.newObject(Repository.class);
        repository.setName("test-repo");
        Module module1 = new Module("test-group", "test-id", "0.25");
        assertFalse(mvnCentralService.isSync("0.25",
                Collections.singletonList(new Project(repository1, Paths.get(""), module1))));
    }
}

