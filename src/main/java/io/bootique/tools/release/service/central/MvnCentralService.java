package io.bootique.tools.release.service.central;

import io.bootique.tools.release.model.maven.Project;

import java.util.List;

public interface MvnCentralService {

    boolean isSync(String version, List<Project> projects);

}
