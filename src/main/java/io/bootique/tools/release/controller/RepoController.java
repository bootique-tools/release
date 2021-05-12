package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.view.RepoView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

@Path("/")
public class RepoController extends BaseController {

    @GET
    public RepoView home() {
        return new RepoView(getCurrentUser(), getCurrentOrganization());
    }

    @GET
    @Path("/checkCache")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean checkCache() {
        AgRequest agRequest = Ag.request(configuration).build();
        DataResponse<Organization> organizations = Ag.select(Organization.class, configuration).request(agRequest).get();
        return !organizations.getObjects().isEmpty() &&
                !getCurrentOrganization().getRepositories().isEmpty();
    }

    @GET
    @Path("repo/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Repository> getAll(@Context UriInfo uriInfo) {
        return Ag.select(Repository.class, configuration).uri(uriInfo).get();
    }
}
