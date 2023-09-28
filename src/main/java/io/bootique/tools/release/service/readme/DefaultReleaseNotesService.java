package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.persistent.ClosedIssue;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.OpenIssue;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.persistent.RepositoryNode;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import java.time.LocalDateTime;
import java.util.List;
import javax.inject.Inject;

public class DefaultReleaseNotesService implements ReleaseNotesService {

    @Inject
    private ServerRuntime cayenneRuntime;

    @Override
    public String createReleaseNotes(String milestoneTitle, boolean todo) {
        ObjectContext context = cayenneRuntime.newContext();
        List<Repository> repositories = ObjectSelect.query(Repository.class)
                .orderBy(Repository.NAME.asc())
                .select(context);

        StringBuilder readme = new StringBuilder();

        for (Repository repository : repositories) {
            List<? extends RepositoryNode> closedIssues = getIssues(milestoneTitle, todo, repository);
            if(closedIssues.isEmpty()) {
               continue;
            }

            readme.append(repository.getName()).append("\n").append("\n");
            for (RepositoryNode issue : closedIssues) {
                String title = issue.getTitle().replaceAll("^\\s+", "");
                readme.append("* #").append(issue.getNumber()).append(" ").append(title).append("\n");
            }
            readme.append("\n");
        }
        return readme.toString();
    }

    private static List<? extends RepositoryNode> getIssues(String milestoneTitle, boolean todo, Repository repository) {
        if(todo) {
            if(milestoneTitle == null || milestoneTitle.isBlank()) {
                return ObjectSelect.query(OpenIssue.class)
                        .where((OpenIssue.REPOSITORY.eq(repository)))
                        .and(OpenIssue.CREATED_AT.gte(LocalDateTime.now().minusYears(1)))
                        .orderBy(OpenIssue.NUMBER.asc())
                        .select(repository.getObjectContext());
            } else {
                return ObjectSelect.query(OpenIssue.class)
                        .where(OpenIssue.MILESTONE.dot(Milestone.TITLE).eq(milestoneTitle))
                        .and(OpenIssue.REPOSITORY.eq(repository))
                        .orderBy(OpenIssue.NUMBER.asc())
                        .select(repository.getObjectContext());
            }
        } else {
            return ObjectSelect.query(ClosedIssue.class)
                    .where(ClosedIssue.MILESTONE.dot(Milestone.TITLE).eq(milestoneTitle))
                    .and(ClosedIssue.REPOSITORY.eq(repository))
                    .orderBy(ClosedIssue.NUMBER.asc())
                    .select(repository.getObjectContext());
        }
    }
}