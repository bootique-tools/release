package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.validation.ValidatePomService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/validation")
public class ValidationController extends BaseJobController {

    @Inject
    private ValidatePomService validatePomService;

    @GET
    @Path("/pom")
    public String validatePom() {
        ObjectContext context = cayenneRuntime.newContext();
        List<Repository> repositories = ObjectSelect.query(Repository.class).select(context);
        Map<String, List<String>> failedRepos = new LinkedHashMap<>();
        for (Repository repository : repositories) {
            if(mavenService.isMavenProject(repository)) {
                failedRepos.putAll(validatePomService.validatePom(repository.getName()));
            }
        }

        if (failedRepos.isEmpty()) {
            return "All poms are valid.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(failedRepos.size()).append(" failed POMs: \n");
        failedRepos.forEach((pom, msgs) -> {
            sb.append(pom).append(":\n");
            msgs.forEach(m -> sb.append("   - ").append(m).append("\n"));
        });
        return sb.toString();
    }

}
