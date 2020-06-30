package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class ValidationView extends BaseView{
    public ValidationView(User user, Organization organization) {
        super("validation", user, organization);
    }
}
