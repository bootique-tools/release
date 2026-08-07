package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.persistent.OpenIssue;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.PullRequest;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.persistent.User;
import io.bootique.tools.release.model.session.SessionResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

@Path("/session")
public class SessionController extends BaseController {

    /**
     * Identity and nav badge counts for the UI shell.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SessionResponse getSession() {

        Organization organization = getCurrentOrganization();
        User user = getCurrentUser();
        ObjectContext context = cayenneRuntime.newContext();

        // aggregate queries rather than Organization.getTotalIssues()/getTotalPRs(), which fault a full to-many
        // list per repository just to call size() -- affordable once per page render, not once per UI mount.
        // upstream repositories carry no issues or PRs and are excluded from the repo list everywhere else.
        long repositories = ObjectSelect.query(Repository.class)
                .where(Repository.UPSTREAM.isFalse())
                .selectCount(context);
        long issues = ObjectSelect.query(OpenIssue.class).selectCount(context);
        long pullRequests = ObjectSelect.query(PullRequest.class).selectCount(context);

        return new SessionResponse(
                organization == null ? null : new SessionResponse.OrganizationInfo(
                        organization.getLogin(), organization.getName(), organization.getUrl()),
                user == null ? null : new SessionResponse.UserInfo(
                        user.getLogin(), user.getName(), user.getUrl()),
                new SessionResponse.Counts((int) repositories, (int) issues, (int) pullRequests));
    }
}
