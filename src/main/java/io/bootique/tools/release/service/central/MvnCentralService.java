package io.bootique.tools.release.service.central;

import io.bootique.tools.release.model.maven.persistent.Project;

import java.util.List;

/**
 * @deprecated since etc.
 */
@Deprecated
public interface MvnCentralService {

    boolean isSync(String version, List<Project> projects);

}
