package io.bootique.tools.release.service.console;

import io.bootique.tools.release.model.persistent.Organization;

import java.util.List;

public interface ConsoleReleaseService {

    boolean checkReadyForRelease(String fromVersion, String releaseVersion, String devVersion, List<String> excludeModules, Organization organization);

    void startReleaseFromConsole();

}
