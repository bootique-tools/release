package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class MavenView extends BaseView {

    public MavenView(User user, Organization organization) {
        super("maven", user, organization);
    }
}
