package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.view.RepoView;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Comparator;

@Path("/")
public class RepoController extends BaseController {

    private static final Comparator<Repository> DEFAULT_COMPARATOR = Repository::compareTo;

    @Inject
    private GitHubApi gitHubApi;

    @GET
    public RepoView home(@QueryParam("filter") String filter, @QueryParam("sort") String sort) {
        Organization organization = gitHubApi.getCurrentOrganization();
        return new RepoView(gitHubApi.getCurrentUser(),
                organization,
                gitHubApi.getRepositories(organization, p -> true, getComparator(sort)),
                preferences.have(GitService.BASE_PATH_PREFERENCE));
    }

    /**
     * Set token logic will redirect here with POST method
     */
    @POST
    public RepoView home() {
        return home(null, null);
    }

    private Comparator<Repository> getComparator(String sort) {
        if(sort == null || sort.isEmpty()) {
            return DEFAULT_COMPARATOR;
        }

        Comparator<Repository> comparator;

        String[] sortSpec = sort.split(":");
        switch (sortSpec[0]) {
            case "title":
                comparator = Comparator.comparing(Repository::getName);
                break;
            case "issue":
                comparator = Comparator.comparing(Repository::getIssuesCount);
                break;
            case "pr":
                comparator = Comparator.comparing(Repository::getPrCount);
                break;
            case "date":
                comparator = Comparator.comparing(Repository::getPushedAt);
                break;
            default:
                return DEFAULT_COMPARATOR;
        }

        if(sortSpec.length > 1 && "desc".equals(sortSpec[1])) {
            comparator = comparator.reversed();
        }

        return comparator;
    }
}
