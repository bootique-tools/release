package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.User;
import io.bootique.tools.release.model.release.ReleaseStage;

public class ReleaseProcessView extends BaseView{

    private ReleaseStage releaseStage;
    private String releaseTitle;
    private String releaseId;
    private String finishStep;

    public ReleaseProcessView(User user, Organization organization, ReleaseStage releaseStage, boolean mode) {
        super("release-process", user, organization);
        this.releaseStage = releaseStage;
        if(releaseStage == ReleaseStage.RELEASE_SYNC || mode) {
            this.finishStep = "none";
        } else {
            this.finishStep = "inline-block";
        }
        this.releaseTitle = releaseStage.getText();
    }

    public String getReleaseTitle(){
        return releaseTitle;
    }

    public String getReleaseId(){
        return releaseStage.name();
    }

    public ReleaseStage getReleaseStage() {
        return releaseStage;
    }

    public String getFinishStep() {
        return finishStep;
    }
}
