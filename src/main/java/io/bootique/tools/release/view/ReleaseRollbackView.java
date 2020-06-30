package io.bootique.tools.release.view;

import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.User;
import io.bootique.tools.release.model.release.RollbackStage;

public class ReleaseRollbackView extends BaseView{

    private RollbackStage rollbackStage;
    private String rollbackTitle;
    private String rollbackId;
    private String finishStep;

    public ReleaseRollbackView(User user, Organization organization, RollbackStage rollbackStage) {
        super("release-rollback", user, organization);
        this.rollbackStage = rollbackStage;
        if(rollbackStage == RollbackStage.ROLLBACK_MVN) {
            this.finishStep = "none";
        } else {
            this.finishStep = "inline-block";
        }
        this.rollbackTitle = rollbackStage.getText();
    }

    public String getRollbackTitle() {
        return rollbackTitle;
    }

    public void setRollbackTitle(String rollbackTitle) {
        this.rollbackTitle = rollbackTitle;
    }

    public String getRollbackId() {
        return rollbackStage.name();
    }

    public String getFinishStep() {
        return finishStep;
    }

    public void setFinishStep(String finishStep) {
        this.finishStep = finishStep;
    }

    public RollbackStage getRollbackStage() {
        return rollbackStage;
    }

    public void setRollbackStage(RollbackStage rollbackStage) {
        this.rollbackStage = rollbackStage;
    }
}
