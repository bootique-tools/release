package io.bootique.tools.release.service.desktop;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

public class MockDesktopService implements DesktopService {

    private Queue<String> nextCommandResult = new ArrayDeque<>();
    private File nextFileResult;

    public void addCommandResult(String result) {
        nextCommandResult.add(result);
    }

    public void setNextFileResult(File file) {
        this.nextFileResult = file;
    }

    @Override
    public File selectFile() {
        return nextFileResult;
    }

    @Override
    public void openFolder(Path path) {
    }

    @Override
    public void openTerminal(Path path) {
    }

    @Override
    public String runCommand(Path path, String... commands) {
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
