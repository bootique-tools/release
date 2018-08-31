package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class ExtraRollbackView extends BaseView {

    private String msg;

    public ExtraRollbackView(User user, Organization organization) {
        super("extra-rollback-view", user, organization);
    }

    public ExtraRollbackView(User user, Organization organization, String msg) {
        super("extra-rollback-view", user, organization);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
