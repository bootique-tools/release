package io.bootique.tools.release.view;

import java.util.List;

import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class IssueView extends BaseView {

    private String filters;
    private String sort;

    public IssueView(User user, Organization organization, String filters, String sort) {
        super("issue", user, organization);
        this.filters = filters;
        this.sort = sort;
    }


    public String getFilters() {
        return filters;
    }

    public String getSort() {
        return sort;
    }
}
