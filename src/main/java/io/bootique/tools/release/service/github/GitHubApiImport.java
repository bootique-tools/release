package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.preferences.Preference;
import io.bootique.tools.release.service.preferences.PreferenceService;

import java.util.List;

public interface GitHubApiImport {

    Preference<String> ORGANIZATION_PREFERENCE = Preference.of("github.organization", String.class);

    Preference<String> AUTH_TOKEN_PREFERENCE = Preference.of("github.auth-token", String.class);

    User getCurrentUser();

    Organization getCurrentOrganization();

    List<Repository> getCurrentRepositoryCollection(Organization organization);

    List<Milestone> getMilestoneCollection(Repository repo);

    List<OpenIssue> getIssueCollection(Repository repo);

    List<ClosedIssue> getClosedIssueCollection(Repository repository);

    List<PullRequest> getPullRequestCollection(Repository repo);
}
