package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.git.GitStatus;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.function.Consumer;

@Path("/git")
public class GitController extends BaseController {

    static final String LOCAL_GIT_PATH_COOKIE = "git-path";

    @Inject
    private DesktopService desktopService;

    @Path("select_path")
    @GET
    public Response selectPath() {
        File file = desktopService.selectFile();
        if(file != null) {
            java.nio.file.Path path = file.toPath();
            preferences.set(GitService.BASE_PATH_PREFERENCE, path);
            NewCookie cookie = new NewCookie(LOCAL_GIT_PATH_COOKIE, path.toString(), "/ui",
                    null, null, -1, false);
            return Response.ok()
                    .entity(path.toString())
                    .cookie(cookie)
                    .build();
        }
        return Response.ok().build();
    }

    @Path("clone")
    @POST
    public Response clone(@QueryParam("repo") String repoName) {
        return checkAndRun(repoName, r -> gitService.clone(r));
    }

    @Path("update")
    @POST
    public Response update(@QueryParam("repo") String repoName) {
        return checkAndRun(repoName, r -> gitService.update(r));
    }

    @Path("update_all")
    @GET
    public Response updateAll() {
        if(!preferences.have(GitService.BASE_PATH_PREFERENCE)) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity("Path should be set via /ui/git/select_path query.")
                    .build();
        }

        Organization organization = getCurrentOrganization();
        organization
                .getRepositories()
                .stream()
                .filter(repo -> gitService.status(repo) != GitStatus.MISSING)
                .forEach(repo -> gitService.update(repo));

        return Response.ok().build();
    }

    @Path("clone_all")
    @POST
    public Response cloneAll() {
        if(!preferences.have(GitService.BASE_PATH_PREFERENCE)) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity("Path should be set via /ui/git/select_path query.")
                    .build();
        }

        Organization organization = getCurrentOrganization();
        organization
                .getRepositories()
                .stream()
                .filter(repo -> gitService.status(repo) == GitStatus.MISSING)
                .forEach(repo -> gitService.clone(repo));

        organization.getObjectContext().commitChanges();
        return Response.ok().build();
    }

    @Path("open")
    @GET
    public Response open(@QueryParam("repo") String repoName, @QueryParam("type") String type) {
        java.nio.file.Path path = preferences.get(GitService.BASE_PATH_PREFERENCE);
        Consumer<Repository> action = "terminal".equals(type)
                ? r -> desktopService.openTerminal(path.resolve(r.getName()))
                : r -> desktopService.openFolder(path.resolve(r.getName()));
        return checkAndRun(repoName, action);
    }

    private Response checkAndRun(String repoName, Consumer<Repository> action) {
        if(!preferences.have(GitService.BASE_PATH_PREFERENCE)) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity("Path should be set via /ui/git/select_path query.")
                    .build();
        }

        ObjectContext context = cayenneRuntime.newContext();
        Repository repository = ObjectSelect.query(Repository.class, Repository.NAME.eq(repoName)).selectFirst(context);
        if(repository == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        action.accept(repository);
        repository.setLocalStatus(GitStatus.OK);
        context.commitChanges();
        return Response.ok().build();
    }

}
