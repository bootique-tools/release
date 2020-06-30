package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Organization;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Organization extends _Organization {

    private static final long serialVersionUID = 1L;

    @JsonProperty("repositories")
    private RepositoryCollection repositoryCollection;

    public RepositoryCollection getRepositoryCollection() {
        return repositoryCollection;
    }

    public void setRepositoryCollection(RepositoryCollection repositoryCollection) {
        this.repositoryCollection = repositoryCollection;
    }

    public void setPRsRepo() {
        repositoryCollection.getRepositories().forEach(repo -> {
            repo.getPullRequestCollection().getPullRequests().forEach(pr -> pr.setRepository(repo));
        });
    }

    @JsonIgnore
    public List<Issue> getIssues(List<Predicate<Issue>> filters, Comparator<Issue> comparator) {
        return getRepositories().stream()
                .flatMap(repository -> repository.getIssues().stream())
                .filter(issue -> filters.stream().allMatch(f -> f.test(issue)))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Milestone> getMilestones() {
        return getRepositories().stream()
                .flatMap(repository -> repository.getMilestones().stream())
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<PullRequest> getPullRequests(Predicate<PullRequest> filter, Comparator<PullRequest> comparator) {
        return getRepositories().stream()
                .flatMap(repository -> repository.getPullRequests().stream())
                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Repository> getRepositories(Predicate<Repository> filter, Comparator<Repository> comparator) {
        return getRepositories().stream()
                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public int getTotalRepos() {
        if (repositoryCollection == null) {
            return getRepositories().size();
        }
        return repositoryCollection.getTotalCount();
    }

    @JsonIgnore
    public int getTotalIssues() {
        int total = 0;
        for (Repository repository : repositoryCollection.getRepositories()) {
            total += repository.getIssuesCount();
        }
        return total;
    }

    @JsonIgnore
    public int getTotalPRs() {
        int total = 0;
        List<Repository> repositoryList;
        if (repositoryCollection == null) {
            repositoryList = getRepositories();
        } else {
            repositoryList = repositoryCollection.getRepositories();
        }
        for (Repository repository : repositoryList) {
            total += repository.getPrCount();
        }
        return total;
    }

    @JsonIgnore
    public void linkRepositories() {
        repositoryCollection.getRepositories().forEach(repo -> repo.setOrganization(this));
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Organization organization = (Organization) o;

        if (this.getId().equals(organization.getId())
                && this.getLogin().equals(organization.getLogin())
                && this.getName().equals(organization.getName())
                && this.getUrl().equals(organization.getUrl())
                && this.getRepositories().size() == organization.getRepositories().size()) {
            return true;
        } else {
            return false;
        }
    }
}
