package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class IssueView extends BaseView {

    private final String filters;
    private final String sort;
    private final String field;

    public IssueView(User user, Organization organization, String sort, String filters, String field) {
        super("issue", user, organization);
        this.filters = filters;
        this.sort = sort == null ? "createdAt" : sort;
        this.field = field;
    }


    public String getFilters() {
        return filters;
    }

    public String getSort() {
        return sort;
    }

    public String getField() {
        return field;
    }
}
