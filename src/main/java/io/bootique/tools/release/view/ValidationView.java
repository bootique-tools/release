package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class ValidationView extends BaseView{
    public ValidationView(User user, Organization organization) {
        super("validation", user, organization);
    }
}
