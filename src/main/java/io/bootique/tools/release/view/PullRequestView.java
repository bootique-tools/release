package io.bootique.tools.release.view;

import java.util.List;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.PullRequest;
import io.bootique.tools.release.model.github.User;

public class PullRequestView extends BaseView {

    private String filter;
    private String sort;

    public PullRequestView(User user, Organization organization, String filter, String sort) {
        super("pr", user, organization);
        this.filter = filter;
        this.sort = sort;
    }


    public String getFilter() {
        return filter;
    }

    public String getSort() {
        return sort;
    }
}
