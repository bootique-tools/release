package io.bootique.tools.release.service.release.descriptors.repository;

import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.ReleaseStageStatus;
import io.bootique.tools.release.model.release.RepositoryDescriptor;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepositoryDescriptorServiceImpl implements RepositoryDescriptorService {

    @Inject
    ServerRuntime cayenneRuntime;

    @Override
    public List<RepositoryDescriptor> createRepositoryDescriptorList(List<Project> projectList) {
        List<RepositoryDescriptor> result = new ArrayList<>();
        projectList.forEach(project -> result.add(createRepositoryDescriptor(project.getRepository())));
        return result;
    }

    public Repository loadRepository(RepositoryDescriptor repositoryDescriptor) {
        return ObjectSelect.query(Repository.class)
                .where(Repository.NAME.eq(repositoryDescriptor.getRepositoryName()))
                .selectFirst(cayenneRuntime.newContext());
    }

    private RepositoryDescriptor createRepositoryDescriptor(Repository repository) {
        Map<ReleaseStage, ReleaseStageStatus> stageStatusMap = initRepositoryStages();
        return new RepositoryDescriptor(repository.getName(), stageStatusMap);
    }

    private Map<ReleaseStage, ReleaseStageStatus> initRepositoryStages() {
        LinkedHashMap<ReleaseStage, ReleaseStageStatus> fullStatusStageMap =
                Arrays.stream(ReleaseStage.values())
                        .collect(Collectors.toMap(
                                key -> key,
                                value -> ReleaseStageStatus.Not_Start,
                                (e1, e2) -> e1,
                                LinkedHashMap::new));
        fullStatusStageMap.remove(ReleaseStage.NO_RELEASE);
        return fullStatusStageMap;
    }

}
