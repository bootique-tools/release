package io.bootique.tools.release.controller;

import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.job.BatchJobService;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;

class DefaultBaseController extends BaseController {

    @Inject
    private BatchJobService jobService;

    void startJob(Function<Project, String> proc, String selectedModules) throws IOException {
        List<Project> allProjects = getSelectedProjects(selectedModules);
        BatchJobDescriptor<Project, String> descriptor = new BatchJobDescriptor<>(allProjects, proc);
        preferences.set(BatchJobService.CURRENT_JOB_ID, jobService.submit(descriptor).getId());
    }

}
