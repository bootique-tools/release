package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.service.preferences.PreferenceService;

import java.nio.file.Path;

public class WindowsDesktopService extends BaseDesktopService {

    public WindowsDesktopService(PreferenceService preferences) {
        super(preferences);
    }

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "cmd.exe", "/c", "start", "cmd");
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        String[] commands = new String[args.length + 1];
        commands[0] = path.toAbsolutePath().resolve("pom.xml").toString();
        System.arraycopy(args, 0, commands, 1, args.length);
        return runCommand(Path.of("."), mavenEnvironment(), "maven.bat", commands);
    }

    @Override
    public String performReleasePlugin(Path path, String operation, String additionalArgs) {
        String[] commands = new String[additionalArgs == null ? 2 : 3];
        commands[0] = path.toAbsolutePath().resolve("pom.xml").toString();
        commands[1] = operation;
        if(additionalArgs != null) {
            commands[2] = additionalArgs;
        }
        return runCommand(Path.of("."), mavenEnvironment(), "maven.bat", commands);
    }
}
