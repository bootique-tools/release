package io.bootique.tools.release.service.central;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Module;
import io.bootique.tools.release.model.maven.Project;
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

    @BeforeEach
    void createService() {
        mvnCentralService = new DefaultMvnCentralService();
        mvnCentralService.targets = targets ->
                ClientBuilder.newClient().target("http://search.maven.org");
    }

    @Test
    @DisplayName("Maven central request test.")
    void testMavenCentralRequest() {
        Repository repository = new Repository();
        repository.setName("bootique-job-parent");
        Module module = new Module("io.bootique.job", "bootique-job-parent", "0.25");
        assertTrue(mvnCentralService.isSync("0.25",
                Collections.singletonList(new Project(repository, Paths.get(""), module))));

        Repository repository1 = new Repository();
        repository.setName("test-repo");
        Module module1 = new Module("test-group", "test-id", "0.25");
        assertFalse(mvnCentralService.isSync("0.25",
                Collections.singletonList(new Project(repository1, Paths.get(""), module1))));
    }
}

