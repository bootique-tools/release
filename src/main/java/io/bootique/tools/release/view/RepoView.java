package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class RepoView extends BaseView {

    public RepoView(User user, Organization organization) {
        super("repo", user, organization);
    }

}
