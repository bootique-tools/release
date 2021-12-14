package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;
import io.bootique.tools.release.model.release.ReleaseStage;

public class ReleaseProcessView extends BaseView {

    public ReleaseProcessView(User user, Organization organization) {
        super("release-process", user, organization);
    }
}
