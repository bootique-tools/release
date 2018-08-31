package io.bootique.tools.release.service.git;

import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.ReleaseService;

import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalGitService implements GitService {

    @Inject
    private DesktopService desktopService;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private ReleaseService releaseService;

    @Override
    public void clone(Repository repository) {
        Path basePath = getBasePathOrThrow();
        Path target = basePath.resolve(repository.getName());
        if(target.toFile().exists()) {
            throw new GitException("Target dir " + target + " already exists, should update instead.");
        }

        desktopService.runCommand(basePath, "git", "clone", repository.getUrl());
    }

    @Override
    public GitStatus status(Repository repository) {
        // Just quick check that repo .git directory exists
        Path target = getBasePathOrThrow().resolve(repository.getName());
        Path targetGit = target.resolve(".git");

        if(!Files.isDirectory(target)) {
            return GitStatus.MISSING;
        }

        if(!Files.isDirectory(targetGit)) {
            return GitStatus.MISSING;
        }

        return GitStatus.OK;
    }

    @Override
    public void update(Repository repository) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        desktopService.runCommand(target, "git", "pull");
    }

    @Override
    public void deleteTag(Repository repository, String releaseVersion) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        desktopService.runCommand(target, "git", "tag", "-d", releaseVersion);
        desktopService.runCommand(target, "git", "push", "origin", ":" + releaseVersion);
    }

    @Override
    public void addAndCommit(Repository repository) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        desktopService.runCommand(target, "git", "add", ".");
        desktopService.runCommand(target, "git", "commit", "-m", "rollback the release of " + releaseService.getReleaseDescriptor().getReleaseVersion());
        desktopService.runCommand(target, "git", "push", "origin", "master");
    }

    private Path getBasePathOrThrow() {
        return preferenceService.get(BASE_PATH_PREFERENCE);
    }

    public void setDesktopService(DesktopService desktopService) {
        this.desktopService = desktopService;
    }

    public void setPreferenceService(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }
}
