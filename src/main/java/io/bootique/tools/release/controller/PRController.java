package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.view.PullRequestView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.function.Predicate;

@Path("/pr")
public class PRController extends BaseController {

    private static final Predicate<PullRequest> DEFAULT_FILTER = pr -> true;

    private static final Comparator<PullRequest> DEFAULT_COMPARATOR = Comparator.comparing(PullRequest::getCreatedAt).reversed();

    @GET
    public PullRequestView home(@QueryParam("filter") String filter, @QueryParam("sort") String sort) {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));
        return new PullRequestView(gitHubApi.getCurrentUser(), organization, filter, sort);
    }

    private Predicate<PullRequest> getPredicate(String filter) {
        if (filter == null || filter.isEmpty()) {
            return DEFAULT_FILTER;
        }

        String[] filterParts = filter.split(":");
        switch (filterParts[0]) {
            case "a":
                return pr -> pr.getAuthor().getLogin().equals(filterParts[1]);
            case "r":
                return pr -> pr.getRepository().getName().equals(filterParts[1]);
            case "l":
                Label label = new Label();
                return pr -> pr.getLabels().contains(label);
            default:
                return DEFAULT_FILTER;
        }
    }

    private Comparator<PullRequest> getComparator(String sort) {
        if (sort == null || sort.isEmpty()) {
            return DEFAULT_COMPARATOR;
        }

        Comparator<PullRequest> comparator;

        String[] sortSpec = sort.split(":");
        switch (sortSpec[0]) {
            case "author":
                comparator = Comparator.comparing(PullRequest::getAuthor);
                break;
            case "title":
                comparator = Comparator.comparing(PullRequest::getTitle);
                break;
            case "repo":
                comparator = Comparator.comparing(PullRequest::getRepository);
                break;
            case "date":
                comparator = Comparator.comparing(PullRequest::getCreatedAt);
                break;
            case "labels":
                comparator = Comparator
                        .<PullRequest>comparingInt(i -> i.getLabels().size()).reversed()
                        .thenComparing(Comparator.comparing(PullRequest::getCreatedAt).reversed());
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
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<PullRequest> showAll(@Context UriInfo uriInfo) {
        DataResponse<PullRequest> pullRequestDataResponse = Ag.select(PullRequest.class, configuration).uri(uriInfo).get();
        return pullRequestDataResponse;
    }
}