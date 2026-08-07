package io.bootique.tools.release.service.desktop;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class MockDesktopService implements DesktopService {

    private Queue<String> nextCommandResult = new ArrayDeque<>();

    private final List<List<String>> invocations = new ArrayList<>();

    public void addCommandResult(String result) {
        nextCommandResult.add(result);
    }

    /** Commands passed to {@link #runCommand}, each as command followed by its arguments. */
    public List<List<String>> getInvocations() {
        return invocations;
    }

    public List<String> lastInvocation() {
        return invocations.get(invocations.size() - 1);
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
        List<String> invocation = new ArrayList<>();
        invocation.add(command);
        invocation.addAll(Arrays.asList(args));
        invocations.add(invocation);

        return nextCommandResult.isEmpty() ? "" : nextCommandResult.remove();
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        if(nextCommandResult.isEmpty()) {
            return "";
        }
        return nextCommandResult.remove();
    }

    @Override
    public String performReleasePlugin(Path path, String operation, String additionalArgs) {
        if(nextCommandResult.isEmpty()) {
            return "";
        }
        return nextCommandResult.remove();
    }
}
