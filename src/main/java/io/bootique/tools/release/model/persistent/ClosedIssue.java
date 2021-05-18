package io.bootique.tools.release.model.persistent;

import io.bootique.tools.release.model.persistent.auto._ClosedIssue;

public class ClosedIssue extends _ClosedIssue {

    private static final long serialVersionUID = 1L;

    public ClosedIssue() {
    }

    public ClosedIssue(OpenIssue issue) {
        setGithubId(issue.getGithubId());
        setUrl(issue.getUrl());
        setNumber(issue.getNumber());
        setTitle(issue.getTitle());
        setCommentsCount(issue.getCommentsCount());
        milestone = issue.getMilestone();
    }
}
