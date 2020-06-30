package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class BranchesView extends BaseView {
    public BranchesView(User user, Organization organization) {
        super("branch", user, organization);
    }
}
