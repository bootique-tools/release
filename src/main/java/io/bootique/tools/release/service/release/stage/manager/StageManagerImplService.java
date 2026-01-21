package io.bootique.tools.release.service.release.stage.manager;

import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import jakarta.inject.Inject;

import java.util.List;

public class StageManagerImplService implements StageManagerService {

    @Inject
    protected ReleaseDescriptorService releaseDescriptorService;

    @Override
    public void dropInProgressStage(RepositoryDescriptor repositoryDescriptor) {
        repositoryDescriptor.getStageStatusMap().forEach((key, value) -> {
            if (value == ReleaseStageStatus.In_Progress) {
                repositoryDescriptor.getStageStatusMap().replace(key, ReleaseStageStatus.Not_Start);
            }
        });
    }

    @Override
    public boolean isStageSyncCurrent(RepositoryDescriptor repositoryDescriptor) {

        List<RepositoryDescriptor> repositoryDescriptorList = releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList();
        int index = repositoryDescriptorList.indexOf(repositoryDescriptor);
        ReleaseStageStatus status = repositoryDescriptor.getStageStatusMap().get(ReleaseStage.values()[ReleaseStage.values().length - 2]);

        return (index == repositoryDescriptorList.size() - 1) && (status == ReleaseStageStatus.Skip ^ status == ReleaseStageStatus.Success);
    }

    @Override
    public boolean releaseHaveFailStage() {
        return releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().stream().anyMatch(
                repositoryDescriptor -> repositoryDescriptor.getStageStatusMap().containsValue(ReleaseStageStatus.Fail)
                        || repositoryDescriptor.getStageStatusMap().containsValue(ReleaseStageStatus.Fail_Rollback)
        );
    }

    @Override
    public boolean releaseHaveRollbackStage() {
        return releaseDescriptorService.getReleaseDescriptor().getRepositoryDescriptorList().stream().anyMatch(
                repositoryDescriptor -> repositoryDescriptor.getStageStatusMap().containsValue(ReleaseStageStatus.Rollback)
        );
    }
}
