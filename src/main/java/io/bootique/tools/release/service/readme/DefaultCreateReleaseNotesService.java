package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.persistent.IssueClose;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;

import java.util.List;

public class DefaultCreateReleaseNotesService implements CreateReleaseNotesService {

    @Override
    public StringBuilder createReleaseNotes(List<Repository> repositories, String milestoneTitle) {
        StringBuilder readme = new StringBuilder();
        for (Repository repository : repositories) {
            for (Milestone milestone : repository.getMilestones()) {
                if (milestone.getTitle().equals(milestoneTitle)) {
                    if (milestone.getIssuesClose() != null && !milestone.getIssuesClose().isEmpty()) {
                        readme.append(repository.getName()).append("\n").append("\n");
                        for (IssueClose issue : milestone.getIssuesClose()) {
                            String title = issue.getTitle().replaceAll("^\\s+", "");
                            readme.append("* #").append(issue.getNumber()).append(" ").append(title).append("\n");
                        }
                        readme.append("\n");
                    }
                }
            }
        }
        return readme;
    }
}