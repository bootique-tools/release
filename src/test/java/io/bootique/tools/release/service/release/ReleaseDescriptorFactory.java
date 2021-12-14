package io.bootique.tools.release.service.release;

import io.bootique.tools.release.model.release.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ReleaseDescriptorFactory {

    static final String fromVersion = "";
    static final String version = "";
    static final String devVersion = "";

    public ReleaseDescriptor createNotStartDescriptor(int repoCount) {
        ReleaseDescriptor releaseDescriptor = createDescriptorWithoutStatusMap(repoCount);
        releaseDescriptor.getRepositoryDescriptorList().forEach( repositoryDescriptor -> {
            LinkedHashMap<ReleaseStage, ReleaseStageStatus> stageStatusMap =
                    Arrays.stream(ReleaseStage.values())
                            .collect(Collectors.toMap(
                                    key -> key,
                                    value -> ReleaseStageStatus.Not_Start,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new));
            stageStatusMap.remove(ReleaseStage.NO_RELEASE);
            repositoryDescriptor.setStageStatusMap(stageStatusMap);
        });
        return releaseDescriptor;
    }

    public ReleaseDescriptor createFinishDescriptor (int repoCount) {
        ReleaseDescriptor releaseDescriptor = createDescriptorWithoutStatusMap(repoCount);
        releaseDescriptor.getRepositoryDescriptorList().forEach( repositoryDescriptor -> {
            LinkedHashMap<ReleaseStage, ReleaseStageStatus> stageStatusMap =
                    Arrays.stream(ReleaseStage.values())
                            .collect(Collectors.toMap(
                                    key -> key,
                                    value -> ReleaseStageStatus.Success,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new));
            stageStatusMap.remove(ReleaseStage.NO_RELEASE);
            repositoryDescriptor.setStageStatusMap(stageStatusMap);
        });
        return releaseDescriptor;
    }

    public ReleaseDescriptor createNotSyncDescriptor(int repoCount) {
        ReleaseDescriptor releaseDescriptor = createDescriptorWithoutStatusMap(repoCount);
        releaseDescriptor.getRepositoryDescriptorList().forEach( repositoryDescriptor -> {
            LinkedHashMap<ReleaseStage, ReleaseStageStatus> stageStatusMap =
                    Arrays.stream(ReleaseStage.values())
                            .collect(Collectors.toMap(
                                    key -> key,
                                    value -> ReleaseStageStatus.Success,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new));
            stageStatusMap.remove(ReleaseStage.NO_RELEASE);
            stageStatusMap.replace(ReleaseStage.RELEASE_SYNC,ReleaseStageStatus.Not_Start);
            repositoryDescriptor.setStageStatusMap(stageStatusMap);
        });
        return releaseDescriptor;
    }

    public ReleaseDescriptor createDescriptorWithoutStatusMap(int repoCount) {
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setReleaseVersions(new ReleaseVersions(fromVersion,version,devVersion));

        List<RepositoryDescriptor> repositoryDescriptorList = new ArrayList<>();
        for (int i = 0; i < repoCount; i++) {

            RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();
            repositoryDescriptor.setRepositoryName("repository" + i);

            repositoryDescriptorList.add(repositoryDescriptor);
        }
        releaseDescriptor.setRepositoryDescriptorList(repositoryDescriptorList);
        return  releaseDescriptor;
    }

}
