package io.bootique.tools.release.view;

import io.bootique.mvc.AbstractView;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public abstract class BaseView extends AbstractView {

    private final Organization organization;

    private final User user;

    public BaseView(String templateName, User user, Organization organization) {
        super(templateName + ".mustache");
        this.organization = organization;
        this.user = user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public User getUser() {
        return user;
    }
}
