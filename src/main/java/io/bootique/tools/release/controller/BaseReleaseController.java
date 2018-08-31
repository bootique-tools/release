package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.util.List;

abstract class BaseReleaseController extends BaseController{

    @Inject
    ReleaseService releaseService;

    @Inject
    LoggerService loggerService;

    ReleaseDescriptor createDescriptor(String prevVersion,
                                       String releaseVersion,
                                       String devVersion,
                                       List<Project> selectedProjects,
                                       ReleaseStage releaseStage,
                                       RollbackStage rollbackStage,
                                       boolean autoMode) {
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor(prevVersion,
                releaseVersion,
                devVersion,
                selectedProjects,
                releaseStage,
                rollbackStage,
                autoMode);

        if(autoMode) {
            releaseService.createThreadForRelease();
        }

        return releaseDescriptor;
    }

    void prepareRelease(ReleaseDescriptor releaseDescriptor) {
        releaseService.prepareRelease();
        releaseService.createReleaseDescriptor(releaseDescriptor);
        loggerService.prepareLogger(releaseDescriptor);
    }

    abstract boolean validate(String releaseVersion, List<Project> selectedProjects);
}
