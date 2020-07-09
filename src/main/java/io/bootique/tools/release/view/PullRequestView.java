package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class PullRequestView extends BaseView {

    private String filter;
    private String sort;
    private String field;

    public PullRequestView(User user, Organization organization, String sort, String filter, String field) {
        super("pr", user, organization);
        this.filter = filter;
        this.sort = sort;
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
