package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.view.RepoView;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.Comparator;

@Path("/")
public class RepoController extends BaseController {

    private static final Comparator<Repository> DEFAULT_COMPARATOR = Repository::compareTo;

    @GET
    public RepoView home(@QueryParam("filter") String filter, @QueryParam("sort") String sort) {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));

        return new RepoView(gitHubApi.getCurrentUser(), organization);
    }

    /**
     * Set token logic will redirect here with POST method
     */
    @POST
    public RepoView home() {
        return home(null, null);
    }

    private Comparator<Repository> getComparator(String sort) {
        if (sort == null || sort.isEmpty()) {
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

        if (sortSpec.length > 1 && "desc".equals(sortSpec[1])) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    @GET
    @Path("/checkCache")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean checkCache() {
        return contentService.haveCache(configuration);
    }

    @GET
    @Path("repo/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Repository> getAll(@Context UriInfo uriInfo) {
        return Ag.select(Repository.class, configuration).uri(uriInfo).get();
    }
}
