package io.bootique.tools.release.service.readme;

import io.bootique.tools.release.model.persistent.IssueClose;
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
            List<IssueClose> closedIssues = ObjectSelect.query(IssueClose.class)
                    .where(IssueClose.MILESTONE.dot(Milestone.TITLE).eq(milestoneTitle))
                    .and(IssueClose.REPOSITORY_ID.eq(Cayenne.intPKForObject(repository)))
                    .orderBy(IssueClose.NUMBER.asc())
                    .select(context);
            if(closedIssues.isEmpty()) {
               continue;
            }

            readme.append(repository.getName()).append("\n").append("\n");
            for (IssueClose issue : closedIssues) {
                String title = issue.getTitle().replaceAll("^\\s+", "");
                readme.append("* #").append(issue.getNumber()).append(" ").append(title).append("\n");
            }
            readme.append("\n");
        }
        return readme.toString();
    }
}