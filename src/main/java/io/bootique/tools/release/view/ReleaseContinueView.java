package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;
import io.bootique.tools.release.model.maven.persistent.Project;
import java.util.List;

public class ReleaseContinueView extends BaseView{

    private String releaseVersion;

    public ReleaseContinueView(User user, Organization organization, String releaseVersion) {
        super("release-continue-view", user, organization);
        this.releaseVersion = releaseVersion;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

}
