package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class MilestonesView extends BaseView {

    public MilestonesView(User user, Organization organization) {
        super("milestone", user, organization);
    }

}
