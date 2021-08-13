package io.bootique.tools.release.service.release.descriptors.repository;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.List;

public interface RepositoryDescriptorService {

    List<RepositoryDescriptor> createRepositoryDescriptorList(List<Project> projectList);

    Repository loadRepository(RepositoryDescriptor repositoryDescriptor);
}
