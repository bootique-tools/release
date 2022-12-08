package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class WindowsDesktopService extends BaseDesktopService {

    @Override
    public void openTerminal(Path path) {
        runCommand(path, "cmd.exe", "/c", "start", "cmd");
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        String[] commands = new String[args.length + 1];
        commands[0] = path.toAbsolutePath().resolve("pom.xml").toString();
        System.arraycopy(args, 0, commands, 1, args.length);
        return runCommand(Path.of("."), "maven.bat", commands);
    }

    @Override
    public String performReleasePlugin(Path path, String operation) {
        String[] commands = new String[2];
        commands[0] = path.toAbsolutePath().resolve("pom.xml").toString();
        commands[1] = operation;
        return runCommand(Path.of("."), "maven.bat", commands);
    }
}
