package io.bootique.tools.release.view;

import java.util.List;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.PullRequest;
import io.bootique.tools.release.model.github.User;

public class PullRequestView extends BaseView {

    private final List<PullRequest> pullRequests;

    public PullRequestView(User user, Organization organization, List<PullRequest> pullRequests) {
        super("pr", user, organization);
        this.pullRequests = pullRequests;
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }
}
