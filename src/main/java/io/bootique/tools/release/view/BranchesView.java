package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class BranchesView extends BaseView {
    public BranchesView(User user, Organization organization) {
        super("branch", user, organization);
    }
}
