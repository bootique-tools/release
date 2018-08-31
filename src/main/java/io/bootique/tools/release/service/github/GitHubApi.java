package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.PullRequest;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.github.User;
import io.bootique.tools.release.service.preferences.Preference;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface GitHubApi {

    Preference<String> ORGANIZATION_PREFERENCE = Preference.of("github.organization", String.class);

    Preference<String> AUTH_TOKEN_PREFERENCE = Preference.of("github.auth-token", String.class);

    User getCurrentUser();

    Organization getCurrentOrganization();

    Organization getOrganization(String name);

    List<Milestone> getMilestones(Organization organization);

    List<Issue> getIssues(Organization organization, Predicate<Issue> predicate, Comparator<Issue> comparator);

    List<PullRequest> getPullRequests(Organization organization, Predicate<PullRequest> predicate, Comparator<PullRequest> comparator);

    List<Repository> getRepositories(Organization organization, Predicate<Repository> predicate, Comparator<Repository> comparator);

    Repository getRepository(String organizationName, String name);

    Milestone createMilestone(Repository repository, String title, String description) throws IOException;

    void closeMilestone(Repository repository, String title, String description);

    void renameMilestone(Repository repository, String title, String description, String newTitle);

    void flushCache(Predicate<String> keyFilter);
}
