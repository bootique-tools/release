package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class ReadmeView extends BaseView {

    public ReadmeView(User user, Organization organization) {
        super("readme-gen", user, organization);
    }
}
