package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class ReleaseView extends BaseView {

    public ReleaseView(User user, Organization organization) {
        super("release", user, organization);
    }
}
