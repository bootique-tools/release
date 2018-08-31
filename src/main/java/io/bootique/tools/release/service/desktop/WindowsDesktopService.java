package io.bootique.tools.release.service.desktop;

import java.nio.file.Path;

public class WindowsDesktopService extends BaseDesktopService {
    @Override
    public void openTerminal(Path path) {
        runCommand(path, "cmd.exe", "/c", "start", "cmd");
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        String[] commands = new String[args.length + 3];
        commands[0] = "cmd.exe";
        commands[1] = "/C";
        commands[2] = "mvn";
        System.arraycopy(args, 0, commands, 3, args.length);
        return runCommand(path, commands);
    }
}
