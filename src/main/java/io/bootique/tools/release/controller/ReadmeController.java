package io.bootique.tools.release.controller;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.bootique.tools.release.model.persistent.*;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.readme.CreateReadmeService;
import io.bootique.tools.release.view.ReadmeView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@Path("/readme")
public class ReadmeController extends BaseController {

    @Inject
    private CreateReadmeService createReadmeService;

    @Inject
    private ContentService contentService;

    @GET
    public ReadmeView home() {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        for (Repository repository : organization.getRepositories()) {
            repository.setIssueCollection(new IssueCollection(repository.getIssues().size(), null));
            repository.setPullRequestCollection(new PullRequestCollection(repository.getPullRequests().size(), null));
            repository.setMilestoneCollection(new MilestoneCollection(repository.getMilestones().size(), null));
        }
        organization.setRepositoryCollection(new RepositoryCollection(organization.getRepositories().size(), organization.getRepositories()));
        return new ReadmeView(gitHubApi.getCurrentUser(), organization);
    }

    @GET
    @Path("/generate")
    public StringBuilder getBranch(@QueryParam("milestoneTitle") String milestoneTitle) {
        return createReadme(milestoneTitle);
    }

    private StringBuilder createReadme(String milestoneTitle) {
        AgRequest agRequest = Ag.request(configuration).build();
        Organization organization = Ag.select(Organization.class, configuration).request(agRequest).get().getObjects().get(0);
        List<Repository> repositories = contentService.getRepositories(organization, p -> true, Repository::compareTo);
        return createReadmeService.createReadme(repositories, milestoneTitle);
    }
}
