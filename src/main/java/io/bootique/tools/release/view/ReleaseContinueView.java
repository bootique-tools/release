package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;
import io.bootique.tools.release.model.maven.Project;

import java.util.List;

public class ReleaseContinueView extends BaseView{

    private String releaseVersion;
    private String currentStage;
    private List<Project> projectList;

    public ReleaseContinueView(User user, Organization organization, String releaseVersion, String currentStage, List<Project> projectList) {
        super("release-continue-view", user, organization);
        this.releaseVersion = releaseVersion;
        this.currentStage = currentStage;
        this.projectList = projectList;
    }


    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public List<Project> getProjectList() {
        return projectList;
    }
}
