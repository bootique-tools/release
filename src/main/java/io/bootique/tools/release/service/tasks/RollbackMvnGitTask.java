package io.bootique.tools.release.service.tasks;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

public class RollbackMvnGitTask implements Function<Repository, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopService.class);

    @Inject
    LoggerService loggerService;

    @Inject
    GitService gitService;

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Inject
    ReleaseDescriptorService releaseDescriptorService;

    @Override
    public String apply(Repository repo) {
        ReleaseDescriptor releaseDescriptor = releaseDescriptorService.getReleaseDescriptor();

        try {
            loggerService.setAppender(repo.getName(), "rollback", String.valueOf(RollbackStage.ROLLBACK_MVN));
            rollbackPom(repo.getName(), releaseDescriptor);

            desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "release:clean"
            );

            gitService.addAndCommit(repo);

            gitService.deleteTag(repo, releaseDescriptor.getReleaseVersions().releaseVersion());
            return "";
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }

    protected void rollbackPom(String repoName, ReleaseDescriptor releaseDescriptor) {

        LOGGER.debug("Rollback from " + releaseDescriptor.getReleaseVersions().devVersion() + " to "
                + releaseDescriptor.getReleaseVersions().fromVersion());
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repoName);
        try(Stream<Path> fileStream = Files.walk(path)) {
            fileStream.filter(Files::isRegularFile)
                    .filter(name -> (name.getFileName().toString().equals("pom.xml") &&
                            !name.toString().contains(File.pathSeparator + "target" + File.pathSeparator)))
                    .forEach(filePath -> {
                        try (Stream<String> lines = Files.lines(filePath)) {
                            List<String> replaced = lines
                                    .map(line -> line.replaceAll("<version>" +
                                                    releaseDescriptor.getReleaseVersions().devVersion() + "</version>",
                                            "<version>" + releaseDescriptor.getReleaseVersions().fromVersion() + "</version>")
                                    )
                                    .collect(Collectors.toList());
                            replaced.forEach(line -> {
                                if (line.contains("<tag>" + releaseDescriptor.getReleaseVersions().releaseVersion() + "</tag>")) {
                                    LOGGER.warn("Project: " + repoName + " contains tag with release version. Use manual mode to rollback this module.");
                                }
                            });
                            Files.write(filePath, replaced);
                        } catch (IOException e) {
                            throw new DesktopException("Can't replace version in pom.xml. ", e);
                        }
                    });
        } catch (IOException e) {
            throw new DesktopException("Can't rollback version on pom.xml", e);
        }
    }
}
