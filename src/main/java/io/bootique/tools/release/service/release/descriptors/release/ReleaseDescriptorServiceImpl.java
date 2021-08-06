package io.bootique.tools.release.service.release.descriptors.release;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.*;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorService;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseDescriptorServiceImpl implements ReleaseDescriptorService {

    @Inject
    private RepositoryDescriptorService repositoryDescriptorService;

    private ReleaseDescriptor releaseDescriptor;

    private final Provider<ReleasePersistentService> releaseSaverServiceProvider;

    @Inject
    public ReleaseDescriptorServiceImpl(Provider<ReleasePersistentService> releaseSaverServiceProvider) {
        this.releaseSaverServiceProvider = releaseSaverServiceProvider;
    }

    @Override
    public void createReleaseDescriptor(ReleaseVersions versions, List<Project> projectList) {
        List<RepositoryDescriptor> repositoriesList = repositoryDescriptorService.createRepositoryDescriptorList(projectList);
        releaseDescriptor = new ReleaseDescriptor(versions, repositoriesList);
    }

    @Override
    public ReleaseDescriptor getReleaseDescriptor() {

        if (releaseDescriptor == null) {

            if (!releaseSaverServiceProvider.get().isReleaseSaved()) {
                return null;
            }
            try {
                releaseDescriptor = releaseSaverServiceProvider.get().loadRelease();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return releaseDescriptor;
    }

    @Override
    public void dropReleaseDescriptor() {
        this.releaseDescriptor = null;
        releaseSaverServiceProvider.get().deleteRelease();
    }

    @Override
    public List<RepositoryDescriptor> getUnfinishedRepositoryDescriptorList() {

        for (ReleaseStage stage : ReleaseStage.values()) {
            var result = releaseDescriptor.getRepositoryDescriptorList().stream().filter(repositoryDescriptor ->
                    repositoryDescriptor.getStageStatusMap().get(stage) == ReleaseStageStatus.Not_Start ^
                            repositoryDescriptor.getStageStatusMap().get(stage) == ReleaseStageStatus.Reload
            ).collect(Collectors.toList());

            if (!result.isEmpty()) {
                return result;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public RepositoryDescriptor getRepositoryDescriptorByName(String name) {
        return releaseDescriptor.getRepositoryDescriptorList()
                .stream()
                .filter(repoDescriptor -> repoDescriptor.getRepositoryName().equals(name))
                .findFirst().get();
    }
}
