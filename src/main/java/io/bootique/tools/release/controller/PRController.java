package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.github.Label;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.PullRequest;
import io.bootique.tools.release.view.PullRequestView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Comparator;
import java.util.function.Predicate;

@Path("/pr")
public class PRController extends BaseController {

    private static final Predicate<PullRequest> DEFAULT_FILTER = pr -> true;

    private static final Comparator<PullRequest> DEFAULT_COMPARATOR = Comparator.comparing(PullRequest::getCreatedAt).reversed();

    @GET
    public PullRequestView home(@QueryParam("filter") String filter, @QueryParam("sort") String sort) {
        Organization organization = gitHubApi.getCurrentOrganization();
        return new PullRequestView(gitHubApi.getCurrentUser(), organization, gitHubApi.getPullRequests(organization, getPredicate(filter), getComparator(sort)));
    }

    private Predicate<PullRequest> getPredicate(String filter) {
        if(filter == null || filter.isEmpty()) {
            return DEFAULT_FILTER;
        }

        String[] filterParts = filter.split(":");
        switch (filterParts[0]) {
            case "a":
                return pr -> pr.getAuthor().getLogin().equals(filterParts[1]);
            case "r":
                return pr -> pr.getRepository().getName().equals(filterParts[1]);
            case "l":
                Label label = new Label(filterParts[1], "");
                return pr -> pr.getLabels().getLabels().contains(label);
            default:
                return DEFAULT_FILTER;
        }
    }

    private Comparator<PullRequest> getComparator(String sort) {
        if(sort == null || sort.isEmpty()) {
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
                        .<PullRequest>comparingInt(i -> i.getLabels().getTotalCount()).reversed()
                        .thenComparing(Comparator.comparing(PullRequest::getCreatedAt).reversed());
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
