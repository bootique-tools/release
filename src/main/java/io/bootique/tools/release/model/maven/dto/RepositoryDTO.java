package io.bootique.tools.release.model.maven.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Repository;

import java.util.ArrayList;
import java.util.List;

public class RepositoryDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("milestones")
    private List<MilestoneDTO> milestones;

    public RepositoryDTO() {
        milestones = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    private void init(Repository repository) {
        this.name = repository.getName();
        this.url = repository.getUrl();
        for (Milestone milestone : repository.getMilestones()) {
            if (milestone.getState().equals("OPEN")) {
                milestones.add(MilestoneDTO.fromModel(milestone));
            }
        }
    }

    private void convertFromDTO(Repository repository) {
        repository.setName(this.name);
        repository.setUrl(this.url);
        List<Milestone> milestoneList = new ArrayList<>();
        for (MilestoneDTO milestoneDTO : this.milestones) {
            milestoneList.add(MilestoneDTO.toModel(milestoneDTO));
        }
        repository.addToMilestonesWithoutContext(milestoneList);
    }

    public static RepositoryDTO fromModel(Repository repository){
        RepositoryDTO repositoryDTO = new RepositoryDTO();
        repositoryDTO.init(repository);
        return repositoryDTO;
    }

    public static Repository toModel(RepositoryDTO repositoryDTO) {
        Repository repository = new Repository();
        repositoryDTO.convertFromDTO(repository);
        return repository;
    }
}
