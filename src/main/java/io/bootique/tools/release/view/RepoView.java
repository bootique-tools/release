package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class RepoView extends BaseView {

    public RepoView(User user, Organization organization) {
        super("repo", user, organization);
    }

}
