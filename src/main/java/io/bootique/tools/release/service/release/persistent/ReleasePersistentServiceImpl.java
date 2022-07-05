package io.bootique.tools.release.service.release.persistent;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ReleasePersistentServiceImpl implements ReleasePersistentService {

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private PreferenceService preferences;

    @Inject
    private ReleaseDescriptorService releaseDescriptorService;


    public void saveRelease() {

        if (!preferences.have(SAVE_PATH)) {
            throw new DesktopException("Can't save release.");
        }

        ReleaseDescriptor releaseDescriptor = releaseDescriptorService.getReleaseDescriptor();

        Path path = Paths.get(preferences.get(SAVE_PATH), releaseDescriptor.getReleaseVersions().releaseVersion());
        Path lockPath = Paths.get(preferences.get(SAVE_PATH), "lock.txt");
        Path pathFile = path.resolve(releaseDescriptor.getReleaseVersions().releaseVersion() + ".json");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            if (!Files.exists(lockPath)) {
                Files.createFile(lockPath);
            }
            Files.write(lockPath, releaseDescriptor.getReleaseVersions().releaseVersion().getBytes());
            if (!Files.exists(pathFile)) {
                Files.createFile(pathFile);
            }
            ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(pathFile.toFile(), releaseDescriptor);
        } catch (IOException e) {
            throw new DesktopException("Can't save release. ", e);
        }
    }

    public boolean isReleaseSaved() {
        Path pathLock = Paths.get(preferences.get(SAVE_PATH), "lock.txt");
        return Files.exists(pathLock);
    }

    public ReleaseDescriptor loadRelease() throws IOException {
        Path pathLock = Paths.get(preferences.get(SAVE_PATH), "lock.txt");
        if (!Files.exists(pathLock)) {
            throw new FileNotFoundException("lock file not exist");
        }

        try {
            List<String> lockList = Files.readAllLines(pathLock);
            if (lockList.size() != 1) {
                throw new DesktopException("Can't load last release.");
            }
            Path path = Paths.get(preferences.get(SAVE_PATH), lockList.get(0), lockList.get(0) + ".json");
            byte[] jsonDescriptor = Files.readAllBytes(path);

            return objectMapper.readValue(jsonDescriptor, ReleaseDescriptor.class);

        } catch (IOException e) {
            throw new DesktopException("Can't load last release. ", e);
        }
    }

    public void deleteRelease() {
        Path pathLock = Paths.get(preferences.get(SAVE_PATH), "lock.txt");

        if (Files.exists(pathLock)) {
            try {
                Files.delete(pathLock);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
