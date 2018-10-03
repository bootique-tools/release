package io.bootique.tools.release.view;

import java.util.List;

import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class IssueView extends BaseView {

    private String defaultFilters;
    private String defaultSort;

    public IssueView(User user, Organization organization, String defaultFilters, String defaultSort) {
        super("issue", user, organization);
        this.defaultFilters = defaultFilters;
        this.defaultSort = defaultSort;
    }


    public String getDefaultFilters() {
        return defaultFilters;
    }

    public String getDefaultSort() {
        return defaultSort;
    }
}
