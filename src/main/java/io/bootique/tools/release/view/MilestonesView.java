package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class MilestonesView extends BaseView {

    public MilestonesView(User user, Organization organization) {
        super("milestone", user, organization);
    }

}
