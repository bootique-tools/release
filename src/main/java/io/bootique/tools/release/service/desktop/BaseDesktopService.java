package io.bootique.tools.release.service.desktop;

import io.bootique.tools.release.service.preferences.PreferenceService;

import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDesktopService implements DesktopService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DesktopService.class);

    private final PreferenceService preferences;

    protected BaseDesktopService(PreferenceService preferences) {
        this.preferences = preferences;
    }

    /**
     * Environment passed to Maven/release subprocesses. When a GPG passphrase is configured it is
     * exposed as {@code MAVEN_GPG_PASSPHRASE} so {@code maven-gpg-plugin} (>= 3.1.0) can sign
     * non-interactively (loopback pinentry). It is intentionally passed via the environment rather
     * than the command line so it never appears in the scripts' echo, {@code logs.log}, or the UI
     * log stream.
     */
    protected Map<String, String> mavenEnvironment() {
        String gpgPassphrase = preferences.get(GPG_PASSPHRASE, null);
        if (gpgPassphrase == null) {
            return Collections.emptyMap();
        }
        Map<String, String> env = new HashMap<>();
        env.put("MAVEN_GPG_PASSPHRASE", gpgPassphrase);
        return env;
    }

    public synchronized File selectFile() {
        JFrame frame = new JFrame("FileChooserDemo");
        FileChooser chooser = new FileChooser();
        try {
            SwingUtilities.invokeAndWait(() -> {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                int width = gd.getDisplayMode().getWidth();
                int height = gd.getDisplayMode().getHeight();
                Point point = new Point(width / 2 - 200, height / 2 - 150);

                chooser.setLocation(point);

                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.add(chooser);
                frame.setLocation(point);
                frame.pack();
                frame.setAlwaysOnTop(true);
                frame.toFront();
                frame.requestFocus();
                frame.setVisible(true);

                chooser.select();
            });
        } catch (Exception e) {
            throw new RuntimeException("Unable to open file chooser", e);
        } finally {
            frame.dispose();
        }

        return chooser.getSelectedFile();
    }

    @Override
    public String runCommand(java.nio.file.Path path, String command, String... args) {
        return runCommand(path, Collections.emptyMap(), command, args);
    }

    protected String runCommand(java.nio.file.Path path, Map<String, String> env, String command, String... args) {
        List<String> commands = new ArrayList<>(1 + args.length);
        commands.add(command);
        commands.addAll(Arrays.asList(args));

        StringBuilder sb = new StringBuilder();
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running command: {}", String.join(" ", commands));
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder()
                    .directory(path.toFile())
                    .command(commands)
                    .redirectErrorStream(true);
            processBuilder.environment().putAll(env);
            Process process = processBuilder.start();

            InputStream stream = process.getInputStream();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            int exitCode = process.waitFor();
            if(exitCode != 0) {
                LOGGER.debug("Exit code: " + exitCode + "\n" + sb);
                throw new DesktopException("Exit code: " + exitCode + "\n" + sb);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.debug("Failed execute command " + String.join(" ", commands), e);
            throw new DesktopException("Failed execute command " + String.join(" ", commands), e);
        }

        LOGGER.debug(sb.toString());

        return sb.toString();
    }

    @Override
    public void openFolder(Path path) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(path.toUri());
            } catch (IOException e) {
                LOGGER.warn("Unable to open " + path, e);
            }
        }
    }

    @Override
    public String runMavenCommand(Path path, String... args) {
        String[] commands = new String[args.length + 2];
        commands[0] = preferences.get(JAVA_HOME);
        commands[1] = path.toAbsolutePath().resolve("pom.xml").toString();
        System.arraycopy(args, 0, commands, 2, args.length);
        return runCommand(Path.of("."), mavenEnvironment(), "./maven.sh", commands);
    }

    /**
     * Perform mvn release:operation, made a separate method to better control and modify parameters.
     *
     * @param path to the directory where to perform release operation
     * @param operation type (prepare, perform, rollback or clean)
     * @return output
     */
    @Override
    public String performReleasePlugin(Path path, String operation, String additionalArgs) {
        String[] commands = new String[additionalArgs == null ? 3 : 4];
        commands[0] = preferences.get(JAVA_HOME);
        commands[1] = path.toAbsolutePath().resolve("pom.xml").toString();
        commands[2] = operation;
        if(additionalArgs != null) {
            commands[3] = additionalArgs;
        }
        return runCommand(Path.of("."), mavenEnvironment(), "./release.sh", commands);
    }
}
