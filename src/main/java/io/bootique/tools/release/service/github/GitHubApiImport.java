package io.bootique.tools.release.service.github;

import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.preferences.Preference;
import io.bootique.tools.release.service.preferences.PreferenceService;

public interface GitHubApiImport {

    Preference<String> ORGANIZATION_PREFERENCE = Preference.of("github.organization", String.class);

    Preference<String> AUTH_TOKEN_PREFERENCE = Preference.of("github.auth-token", String.class);

    User getCurrentUser();

    PreferenceService getPreferences();

    Organization getCurrentOrganization();

    RepositoryCollection getCurrentRepositoryCollection(Organization organization);

    MilestoneCollection getMilestoneCollection(Repository repo);

    IssueCollection getIssueCollection(Repository repo);

    PullRequestCollection getPullRequestCollection(Repository repo);
}
