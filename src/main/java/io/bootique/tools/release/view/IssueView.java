package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;

public class IssueView extends BaseView {

    private String filters;
    private String sort;
    private String field;

    public IssueView(User user, Organization organization, String sort, String filters, String field) {
        super("issue", user, organization);
        this.filters = filters;
        this.sort = sort;
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
