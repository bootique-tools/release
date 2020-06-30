package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class ReadmeView extends BaseView {

    public ReadmeView(User user, Organization organization) {
        super("readme-gen", user, organization);
    }
}
