package io.bootique.tools.release.view;

import java.util.List;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.github.User;

public class RepoView extends BaseView {

    private final List<Repository> repositories;
    private final boolean gitPathSet;

    public RepoView(User user, Organization organization, List<Repository> repositories, boolean gitPathSet) {
        super("repo", user, organization);
        this.repositories = repositories;
        this.gitPathSet = gitPathSet;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public boolean isGitPathSet() {
        return gitPathSet;
    }
}
