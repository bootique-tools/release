package io.bootique.tools.release.model.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * "organization": {
 *   "id": "MDEyOk9yZ2FuaXphdGlvbjE4MDA4MTU1",
 *   "name": "Bootique Project",
 *   "repositories": {
 *     "totalCount": 31,
 *     "nodes": []
 *   }
 * }
 */
public class Organization extends GitHubEntity {

    private String name;

    private String login;

    @JsonProperty("repositories")
    private RepositoryCollection repositoryCollection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public RepositoryCollection getRepositoryCollection() {
        return repositoryCollection;
    }

    public void setRepositoryCollection(RepositoryCollection repositoryCollection) {
        this.repositoryCollection = repositoryCollection;
    }

    public void setIssuesRepo() {
        repositoryCollection.getRepositories().forEach(repo -> {
            repo.getIssueCollection().getIssues().forEach(issue -> issue.setRepository(repo));
        });
    }

    public void setPRsRepo() {
        repositoryCollection.getRepositories().forEach(repo -> {
            repo.getPullRequestCollection().getPullRequests().forEach(pr -> pr.setRepository(repo));
        });
    }

    public void setMilestonesRepo() {
        repositoryCollection.getRepositories().forEach(repo -> {
            repo.getMilestoneCollection().getMilestones().forEach(milestone -> milestone.setRepository(repo));
        });
    }

    @JsonIgnore
    public List<Issue> getIssues(List<Predicate<Issue>> filters, Comparator<Issue> comparator) {
        return repositoryCollection.getRepositories().stream()
                .flatMap(r -> r.getIssueCollection().getIssues().stream())
                .filter(issue -> filters.stream().allMatch(f -> f.test(issue)))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Milestone> getMilestones() {
        return repositoryCollection.getRepositories().stream()
                .flatMap(r -> r.getMilestoneCollection().getMilestones().stream())
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<PullRequest> getPullRequests(Predicate<PullRequest> filter, Comparator<PullRequest> comparator) {
        return repositoryCollection.getRepositories().stream()
                .flatMap(r -> r.getPullRequestCollection().getPullRequests().stream())
                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Repository> getRepositories(Predicate<Repository> filter, Comparator<Repository> comparator) {
        return repositoryCollection.getRepositories().stream()
                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public int getTotalRepos() {
        return repositoryCollection.getTotalCount();
    }

    @JsonIgnore
    public int getTotalIssues() {
        int total = 0;
        for(Repository repository : repositoryCollection.getRepositories()) {
            total += repository.getIssuesCount();
        }
        return total;
    }

    @JsonIgnore
    public int getTotalPRs() {
        int total = 0;
        for(Repository repository : repositoryCollection.getRepositories()) {
            total += repository.getPrCount();
        }
        return total;
    }

    @JsonIgnore
    public void linkRepositories() {
        repositoryCollection.getRepositories().forEach(repo -> repo.setOrganization(this));
    }
}
