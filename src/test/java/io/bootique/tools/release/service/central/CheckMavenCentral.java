package io.bootique.tools.release.service.central;

import io.bootique.jersey.client.HttpTargets;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.BeforeEach;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Set;



/**
 * @deprecated need to be refactoring for new model of project. After removing module entity (and rootModule from project),
 * the is no gitHubId property for query.
 */
@Deprecated
class CheckMavenCentral {

    private DefaultMvnCentralService mvnCentralService;

    private ObjectContext context;

    @BeforeEach
    void createService() {
        mvnCentralService = new DefaultMvnCentralService();
        mvnCentralService.targets = new HttpTargets() {
            @Override
            public WebTarget newTarget(String targetName) {
                return ClientBuilder.newClient().target("http://search.maven.org");
            }

            @Override
            public Set<String> getTargetNames() {
                return Set.of("test");
            }
        };

        ServerRuntime cayenneRuntime = ServerRuntime.builder()
                .addConfig("cayenne/cayenne-project.xml")
                .build();
        context = cayenneRuntime.newContext();
    }

//    @Test
//    @DisplayName("Maven central request test.")
//    void testMavenCentralRequest() {
//        Repository repository = context.newObject(Repository.class);
//        repository.setName("bootique-job-parent");
//        Module module = new Module("io.bootique.job", "bootique-job-parent", "0.25");
//        assertTrue(mvnCentralService.isSync("0.25",
//                Collections.singletonList(new Project(repository, Paths.get(""), module))));
//
//        Repository repository1 = context.newObject(Repository.class);
//        repository.setName("test-repo");
//        Module module1 = new Module("test-group", "test-id", "0.25");
//        assertFalse(mvnCentralService.isSync("0.25",
//                Collections.singletonList(new Project(repository1, Paths.get(""), module1))));
//    }
}

