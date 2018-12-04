package io.bootique.tools.release.model.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * "id": "MDk6TWlsZXN0b25lMjgzMjkwOA==",
 * "title": "0.25",
 * "number": 18
 */
public class Milestone extends GitHubEntity implements Comparable<Milestone> {

    private String title;
    private int number;
    private String state;

    @JsonIgnore
    private Repository repository;

    @JsonProperty("issuesList")
    private List<Issue> issues = new ArrayList<>();

    @JsonProperty("issues")
    private IssueCollection issueCollection;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void addIssue(Issue issue) {
        issues.add(issue);
    }

    public void addAllIssues(Collection<Issue> issues) {
        this.issues.addAll(issues);
    }

    @JsonProperty("issuesList")
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    @JsonProperty("issuesList")
    public List<Issue> getIssues() {
        return issues;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public int compareTo(Milestone o) {
        return title.compareTo(o.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Milestone milestone = (Milestone) o;
        return title.equals(milestone.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return "{milestone " + title + '}';
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("issues")
    public IssueCollection getIssueCollection() {
        return issueCollection;
    }

    @JsonProperty("issues")
    public void setIssueCollection(IssueCollection issueCollection) {
        this.issueCollection = issueCollection;
    }
}
