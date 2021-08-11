package io.bootique.tools.release.service.release.descriptors.release;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.*;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorService;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReleaseDescriptorServiceImpl implements ReleaseDescriptorService {

    @Inject
    protected RepositoryDescriptorService repositoryDescriptorService;

    protected ReleaseDescriptor releaseDescriptor;

    final Provider<ReleasePersistentService> persistentServiceProvider;

    @Inject
    public ReleaseDescriptorServiceImpl(Provider<ReleasePersistentService> persistentServiceProvider) {
        this.persistentServiceProvider = persistentServiceProvider;
    }

    public void setReleaseDescriptor(ReleaseDescriptor releaseDescriptor) {
        this.releaseDescriptor = releaseDescriptor;
    }

    @Override
    public void createReleaseDescriptor(ReleaseVersions versions, List<Project> projectList) {
        List<RepositoryDescriptor> repositoriesList = repositoryDescriptorService.createRepositoryDescriptorList(projectList);
        releaseDescriptor = new ReleaseDescriptor(versions, repositoriesList);
    }

    @Override
    public ReleaseDescriptor getReleaseDescriptor() {

        if (releaseDescriptor == null) {

            if (!persistentServiceProvider.get().isReleaseSaved()) {
                return null;
            }
            try {
                releaseDescriptor = persistentServiceProvider.get().loadRelease();
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
        persistentServiceProvider.get().deleteRelease();
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
