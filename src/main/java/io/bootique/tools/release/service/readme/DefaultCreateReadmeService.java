package io.bootique.tools.release.service.readme;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.bootique.tools.release.model.github.Issue;
import io.bootique.tools.release.model.github.IssueCollection;
import io.bootique.tools.release.model.github.Milestone;
import io.bootique.tools.release.model.github.MilestoneCollection;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.github.GitHubApi;

import java.util.List;

public class DefaultCreateReadmeService implements CreateReadmeService {

    @Inject
    @Named("updateCache")
    private GitHubApi gitHubApi;

    @Override
    public StringBuilder createReadme(List<Repository> repositories, String milestoneTitle) {
        StringBuilder readme = new StringBuilder();
        for(Repository repository : repositories) {
            MilestoneCollection milestoneCollection = gitHubApi.getMilestoneCollection(repository);
            for(Milestone milestone : milestoneCollection.getMilestones()) {
                if(milestone.getTitle().equals(milestoneTitle)) {
                    IssueCollection issueCollection = gitHubApi.getClosedIssueCollection(repository, milestone.getNumber());
                    if(issueCollection != null && !issueCollection.getIssues().isEmpty()) {
                        readme.append(repository.getName()).append("\n").append("\n");
                        for(Issue issue : issueCollection.getIssues()) {
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
