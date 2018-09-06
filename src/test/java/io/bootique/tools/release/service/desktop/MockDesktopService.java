package io.bootique.tools.release.service.desktop;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

public class MockDesktopService implements DesktopService {

    private Queue<String> nextCommandResult = new ArrayDeque<>();

    public void addCommandResult(String result) {
        nextCommandResult.add(result);
    }

    @Override
    public File selectFile() {
        return null;
    }

    @Override
    public void openFolder(Path path) {
    }

    @Override
    public void openTerminal(Path path) {
    }

    @Override
    public String runCommand(Path path, String command, String... args) {
        return nextCommandResult.remove();
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        if(nextCommandResult.isEmpty()) {
            return "";
        }
        return nextCommandResult.remove();
    }
}
