package io.bootique.tools.release.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.auto._Organization;

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

    @JsonIgnore
    public int getTotalRepos() {
        return getRepositories().size();
    }

    @JsonIgnore
    public int getTotalIssues() {
        int total = 0;
        for (Repository repository : getRepositories()) {
            total += repository.getIssues().size();
        }
        return total;
    }

    @JsonIgnore
    public int getTotalPRs() {
        int total = 0;
        for (Repository repository : getRepositories()) {
            total += repository.getPullRequests().size();
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

        if (this.getGithubId().equals(organization.getGithubId())
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
