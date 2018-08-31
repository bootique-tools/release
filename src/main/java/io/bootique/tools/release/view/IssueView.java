package io.bootique.tools.release.view;

import java.util.List;

import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;

public class IssueView extends BaseView {

    private final List<Issue> issues;

    public IssueView(User user, Organization organization, List<Issue> issues) {
        super("issue", user, organization);
        this.issues = issues;
    }

    public List<Issue> getIssues() {
        return issues;
    }
}
