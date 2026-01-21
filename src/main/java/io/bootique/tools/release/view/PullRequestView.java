package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class PullRequestView extends BaseView {

    private final String filter;
    private final String sort;
    private final String field;

    public PullRequestView(User user, Organization organization, String sort, String filter, String field) {
        super("pr", user, organization);
        this.filter = filter;
        this.sort = sort == null ? "createdAt" : sort;
        this.field = field;
    }

    public String getFilter() {
        return filter;
    }

    public String getSort() {
        return sort;
    }

    public String getField() {
        return field;
    }
}
