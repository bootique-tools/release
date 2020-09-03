package io.bootique.tools.release.model.persistent;

import io.bootique.tools.release.model.persistent.auto._IssueClose;

public class IssueClose extends _IssueClose {

    private static final long serialVersionUID = 1L;

    public IssueClose() {
    }

    public IssueClose(IssueOpen issue) {
        setGithubId(issue.getGithubId());
        setUrl(issue.getUrl());
        setNumber(issue.getNumber());
        setTitle(issue.getTitle());
        milestone = issue.getMilestone();
    }
}
