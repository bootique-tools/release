package io.bootique.tools.release.service.git;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

public class ExternalGitService implements GitService {

    @Inject
    protected DesktopService desktopService;

    @Inject
    protected PreferenceService preferenceService;

    @Inject
    protected ReleaseDescriptorService releaseDescriptorService;

    @Override
    public void clone(Repository repository) {
        Path basePath = getBasePathOrThrow();
        Path target = basePath.resolve(repository.getName());
        if(target.toFile().exists()) {
            throw new GitException("Target dir " + target + " already exists, should update instead.");
        }

        desktopService.runCommand(basePath, "git", "clone", repository.getUrl());
        repository.setLocalStatus(GitStatus.OK);
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
        desktopService.runCommand(target, "git", "commit", "-m", "rollback the release of "
                + releaseDescriptorService.getReleaseDescriptor().getReleaseVersions().releaseVersion());
        desktopService.runCommand(target, "git", "push", "origin", "master");
    }

    @Override
    public void createBranch(Repository repository, String branchTitle) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        desktopService.runCommand(target, "git", "checkout", "-b", branchTitle);
        desktopService.runCommand(target, "git", "push", "-u", "origin", branchTitle);
    }

    @Override
    public String getCurrentBranchName(Repository repository) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        return desktopService.runCommand(target, "git", "rev-parse", "--abbrev-ref", "HEAD").replace("\n", "");
    }

    @Override
    public boolean isClean(Repository repository) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        return desktopService.runCommand(target, "git", "status").contains("nothing to commit, working tree clean");
    }

    public String[] getBranches(Repository repository) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        return desktopService.runCommand(target, "git", "branch","-a").split("\n");
    }

    @Override
    public String checkoutBranch(Repository repository, String branchTitle) {
        Path target = getBasePathOrThrow().resolve(repository.getName());
        String[] branches = getBranches(repository);
        for(String branch : branches) {
            branch = branch.replaceAll("\\s","");
            branch = branch.replaceAll("remotes/origin/","");
            branch = branch.startsWith("*") ? branch.substring(1) : branch;
            if(branch.equals(branchTitle)) {
                if(this.isClean(repository)) {
                    return desktopService.runCommand(target, "git", "checkout", branchTitle);
                } else {
                    throw new DesktopException("You have uncommited changes in " + repository.getName());
                }
            }
        }
        throw new DesktopException("Error while checkout to " + branchTitle + " in " + repository.getName() + ". Branch not found.");
    }

    private Path getBasePathOrThrow() {
        return preferenceService.get(BASE_PATH_PREFERENCE);
    }

}
