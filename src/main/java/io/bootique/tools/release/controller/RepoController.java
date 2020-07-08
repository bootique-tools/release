package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.view.RepoView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

@Path("/")
public class RepoController extends BaseController {

    @GET
    public RepoView home(@Context UriInfo uriInfo) {
        Organization organization = Ag.select(Organization.class, configuration).uri(uriInfo).get().getObjects().get(0);
        return new RepoView(gitHubApi.getCurrentUser(), organization);
    }

    @GET
    @Path("/checkCache")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean checkCache() {
        return contentService.haveCache(configuration);
    }

    @GET
    @Path("repo/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Repository> getAll(@Context UriInfo uriInfo) {
        return Ag.select(Repository.class, configuration).uri(uriInfo).get();
    }
}
