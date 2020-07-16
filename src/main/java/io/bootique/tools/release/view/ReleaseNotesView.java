package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class ReleaseNotesView extends BaseView {

    public ReleaseNotesView(User user, Organization organization) {
        super("release-notes-gen", user, organization);
    }
}
