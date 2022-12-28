package io.bootique.tools.release.service.release.descriptors.release;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseVersions;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.List;

public interface ReleaseDescriptorService {

    void createReleaseDescriptor(ReleaseVersions versions, List<Project> projectList);

    ReleaseDescriptor getReleaseDescriptor();

    void dropReleaseDescriptor();

    List<RepositoryDescriptor> getUnfinishedRepositoryDescriptorList(ReleaseStage stage);

    RepositoryDescriptor getRepositoryDescriptorByName(String name);

}
