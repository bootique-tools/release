package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.Label;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.view.IssueView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Path("/issue")
public class IssueController extends BaseController {

    @Inject
    private ContentService contentService;

    private static final Predicate<Issue> DEFAULT_FILTER = issue -> true;

    private static final Comparator<Issue> DEFAULT_COMPARATOR = Comparator.comparing(Issue::getCreatedAt).reversed();

    @GET
    public IssueView home(@QueryParam("filter") String filters, @QueryParam("sort") String sort) {
        Organization organization = gitHubApi.getCurrentOrganization();
        return new IssueView(gitHubApi.getCurrentUser(), organization, filters, sort);
    }

    private List<Predicate<Issue>> getPredicates(String filters) {
        String[] filtersArr = filters.split(",");
        List<Predicate<Issue>> predicates = new ArrayList<>();
        for (String filter : filtersArr) {
            predicates.add(getPredicate(filter));
        }
        return predicates;
    }

    private Predicate<Issue> getPredicate(String filter) {
        if(filter == null || filter.isEmpty()) {
            return DEFAULT_FILTER;
        }

        String[] filterParts = filter.split(":");
        switch (filterParts[0]) {
            case "a":
                return issue -> issue.getAuthor().getLogin().equals(filterParts[1]);
            case "m":
                return issue -> issue.getMilestone() != null && issue.getMilestone().getTitle().equals(filterParts[1]);
            case "r":
                return issue -> issue.getRepository().getName().equals(filterParts[1]);
            case "l":
                Label label = new Label(filterParts[1], "");
                return issue -> issue.getLabels().getLabels().contains(label);
            default:
                return DEFAULT_FILTER;
        }
    }

    private Comparator<Issue> getComparator(String sort) {
        if(sort == null || sort.isEmpty()) {
            return DEFAULT_COMPARATOR;
        }

        Comparator<Issue> comparator;

        String[] sortSpec = sort.split(":");
        switch (sortSpec[0]) {
            case "author":
                comparator = Comparator.comparing(Issue::getAuthor);
                break;
            case "title":
                comparator = Comparator.comparing(Issue::getTitle);
                break;
            case "repo":
                comparator = Comparator.comparing(issue -> issue.getRepository().getName());
                break;
            case "milestone":
                comparator = (i1, i2) -> {
                    Milestone m1 = i1.getMilestone();
                    Milestone m2 = i2.getMilestone();
                    if(m1 == null && m2 == null) {
                        return i1.getCreatedAt().compareTo(i2.getCreatedAt());
                    }
                    if(m1 == null) {
                        return 1;
                    }
                    if(m2 == null) {
                        return -1;
                    }
                    return m1.compareTo(m2);
                };
                break;
            case "labels":
                comparator = Comparator
                        .<Issue>comparingInt(i -> i.getLabels().getTotalCount()).reversed()
                        .thenComparing(Comparator.comparing(Issue::getCreatedAt).reversed());
                break;
            case "date":
                comparator = Comparator.comparing(Issue::getCreatedAt);
                break;
            default:
                return DEFAULT_COMPARATOR;
        }

        if(sortSpec.length > 1 && "desc".equals(sortSpec[1])) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    @GET
    @Path("/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Issue> showAll(@QueryParam("filter") String filters, @QueryParam("sort") String sort) {
        Organization organization = gitHubApi.getCurrentOrganization();
        return contentService.getIssues(organization, getPredicates(filters), getComparator(sort));
    }
}
