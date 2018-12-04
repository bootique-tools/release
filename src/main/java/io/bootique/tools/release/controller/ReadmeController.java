package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.readme.CreateReadmeService;
import io.bootique.tools.release.view.ReadmeView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("/readme")
public class ReadmeController extends BaseController{

    @Inject
    private CreateReadmeService createReadmeService;

    @Inject
    private ContentService contentService;

    @GET
    public ReadmeView home() {
        return new ReadmeView(gitHubApi.getCurrentUser(), gitHubApi.getCurrentOrganization());
    }

    @GET
    @Path("/generate")
    public StringBuilder getBranch(@QueryParam("milestoneTitle") String milestoneTitle) {
        return createReadme(milestoneTitle);
    }

    private StringBuilder createReadme(String milestoneTitle) {
        Organization organization = gitHubApi.getCurrentOrganization();
        List<Repository> repositories = contentService.getRepositories(organization, p -> true, Repository::compareTo);
        return createReadmeService.createReadme(repositories, milestoneTitle);
    }
}
