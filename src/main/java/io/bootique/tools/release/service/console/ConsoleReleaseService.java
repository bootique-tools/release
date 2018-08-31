package io.bootique.tools.release.service.console;

import java.util.List;

public interface ConsoleReleaseService {

    boolean checkReadyForRelease(String fromVersion, String releaseVersion, String devVersion, List<String> excludeModules);

    void startReleaseFromConsole();

}
