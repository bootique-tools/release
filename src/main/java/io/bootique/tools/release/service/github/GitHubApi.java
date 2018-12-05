package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.github.*;
import io.bootique.tools.release.service.preferences.Preference;

public interface GitHubApi {

    Preference<String> ORGANIZATION_PREFERENCE = Preference.of("github.organization", String.class);

    Preference<String> AUTH_TOKEN_PREFERENCE = Preference.of("github.auth-token", String.class);

    User getCurrentUser();

    Organization getCurrentOrganization();

    MilestoneCollection getMilestoneCollection(Repository repo);

    IssueCollection getIssueCollection(Repository repo);

    IssueCollection getClosedIssueCollection(Repository repository, int id);

    PullRequestCollection getPullRequestCollection(Repository repo);
}
