package io.bootique.tools.release.controller;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.view.RepoView;
import org.apache.cayenne.query.ObjectSelect;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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
        return ObjectSelect.columnQuery(Organization.class, Organization.REPOSITORIES.count())
                .selectOne(cayenneRuntime.newContext()) > 0;
    }

    @GET
    @Path("repo/show-all")
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse<Repository> getAll(@Context UriInfo uriInfo) {
        return AgJaxrs.select(Repository.class, configuration)
                .request(AgJaxrs.request(configuration).andExp("upstream = false").build())
                .clientParams(uriInfo.getQueryParameters())
                .get();
    }
}
