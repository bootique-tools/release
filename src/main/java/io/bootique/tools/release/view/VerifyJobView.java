package io.bootique.tools.release.view;

import io.bootique.tools.release.model.github.Organization;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.github.User;
import io.bootique.tools.release.model.job.BatchJob;

public class VerifyJobView extends BaseView {

    private final BatchJob<Repository, String> job;

    public VerifyJobView(BatchJob<Repository, String> job, User user, Organization organization) {
        super("verify-job", user, organization);
        this.job = job;
    }

    public BatchJob<Repository, String> getJob() {
        return job;
    }
}
