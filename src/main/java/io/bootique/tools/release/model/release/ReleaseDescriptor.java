package io.bootique.tools.release.model.release;

import java.util.List;

public class ReleaseDescriptor {

    private ReleaseVersions releaseVersions;
    private List<RepositoryDescriptor> repositoryDescriptorList;

    public ReleaseDescriptor() {
    }

    public ReleaseDescriptor(ReleaseVersions releaseVersions, List<RepositoryDescriptor> repositoryDescriptorList) {
        this.releaseVersions = releaseVersions;
        this.repositoryDescriptorList = repositoryDescriptorList;
    }

    public ReleaseVersions getReleaseVersions() {
        return releaseVersions;
    }

    public void setReleaseVersions(ReleaseVersions releaseVersions) {
        this.releaseVersions = releaseVersions;
    }

    public List<RepositoryDescriptor> getRepositoryDescriptorList() {
        return repositoryDescriptorList;
    }

    public void setRepositoryDescriptorList(List<RepositoryDescriptor> repositoryDescriptorList) {
        this.repositoryDescriptorList = repositoryDescriptorList;
    }
}

