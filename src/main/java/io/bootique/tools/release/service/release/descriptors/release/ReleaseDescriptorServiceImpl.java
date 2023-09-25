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
import java.util.Objects;
import java.util.stream.Collectors;

public class ReleaseDescriptorServiceImpl implements ReleaseDescriptorService {

    public void setRepositoryDescriptorService(RepositoryDescriptorService repositoryDescriptorService) {
        this.repositoryDescriptorService = repositoryDescriptorService;
    }

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
    public List<RepositoryDescriptor> getUnfinishedRepositoryDescriptorList(ReleaseStage stage) {
        if (stage == ReleaseStage.RELEASE_PREPARE || stage == ReleaseStage.RELEASE_PERFORM) {
            return getLastUnfinishedRepositoryDescriptors(List.of(ReleaseStage.RELEASE_PREPARE, ReleaseStage.RELEASE_PERFORM));
        } else {
            return getFirstUnfinishedRepositoryDescriptorList();
        }
    }

    private List<RepositoryDescriptor> getFirstUnfinishedRepositoryDescriptorList() {
        for (ReleaseStage stage : ReleaseStage.values()) {
            List<RepositoryDescriptor> result = getUnfinishedRepositoryDescriptorsByStage(stage);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return Collections.emptyList();
    }

    private List<RepositoryDescriptor> getLastUnfinishedRepositoryDescriptors(List<ReleaseStage> stages) {
        List<RepositoryDescriptor> unfinishedRepoDescriptorList = null;
        for (ReleaseStage stage : stages) {
            List<RepositoryDescriptor> descriptorList = getUnfinishedRepositoryDescriptorsByStage(stage);
            if (!descriptorList.isEmpty()) {
                unfinishedRepoDescriptorList = descriptorList;
            }
        }
        return Objects.requireNonNullElse(unfinishedRepoDescriptorList, Collections.emptyList());
    }

    private List<RepositoryDescriptor> getUnfinishedRepositoryDescriptorsByStage(ReleaseStage stage) {
        return releaseDescriptor.getRepositoryDescriptorList().stream().filter(repositoryDescriptor ->
                repositoryDescriptor.getStageStatusMap().get(stage) == ReleaseStageStatus.Not_Start ^
                        repositoryDescriptor.getStageStatusMap().get(stage) == ReleaseStageStatus.Reload
        ).collect(Collectors.toList());
    }

    @Override
    public RepositoryDescriptor getRepositoryDescriptorByName(String name) {
        return releaseDescriptor.getRepositoryDescriptorList()
                .stream()
                .filter(repoDescriptor -> repoDescriptor.getRepositoryName().equals(name))
                .findFirst().get();
    }

}
