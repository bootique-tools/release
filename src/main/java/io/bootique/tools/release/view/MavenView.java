package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class MavenView extends BaseView {

    public MavenView(User user, Organization organization) {
        super("maven", user, organization);
    }
}
