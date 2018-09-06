package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class WindowsDesktopService extends BaseDesktopService {
    @Override
    public void openTerminal(Path path) {
        runCommand(path, "cmd.exe", "/c", "start", "cmd");
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        String[] commands = new String[args.length + 2];
        commands[0] = "/C";
        commands[1] = "mvn";
        System.arraycopy(args, 0, commands, 2, args.length);
        return runCommand(path, "cmd.exe", commands);
    }
}
