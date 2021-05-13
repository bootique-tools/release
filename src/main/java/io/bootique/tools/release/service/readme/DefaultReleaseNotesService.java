package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.persistent.ClosedIssue;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import java.util.List;
import javax.inject.Inject;

public class DefaultReleaseNotesService implements ReleaseNotesService {

    @Inject
    private ServerRuntime cayenneRuntime;

    @Override
    public String createReleaseNotes(String milestoneTitle) {
        ObjectContext context = cayenneRuntime.newContext();
        List<Repository> repositories = ObjectSelect.query(Repository.class)
                .orderBy(Repository.NAME.asc())
                .select(context);

        StringBuilder readme = new StringBuilder();

        for (Repository repository : repositories) {
            List<ClosedIssue> closedIssues = ObjectSelect.query(ClosedIssue.class)
                    .where(ClosedIssue.MILESTONE.dot(Milestone.TITLE).eq(milestoneTitle))
                    .and(ClosedIssue.REPOSITORY_ID.eq(Cayenne.intPKForObject(repository)))
                    .orderBy(ClosedIssue.NUMBER.asc())
                    .select(context);
            if(closedIssues.isEmpty()) {
               continue;
            }

            readme.append(repository.getName()).append("\n").append("\n");
            for (ClosedIssue issue : closedIssues) {
                String title = issue.getTitle().replaceAll("^\\s+", "");
                readme.append("* #").append(issue.getNumber()).append(" ").append(title).append("\n");
            }
            readme.append("\n");
        }
        return readme.toString();
    }
}