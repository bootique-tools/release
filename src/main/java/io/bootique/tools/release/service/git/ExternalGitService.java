package io.bootique.tools.release.service.git;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalGitService implements GitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);

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

        // SSH matches the scm:git:ssh:// developerConnection the released POMs declare, so the working
        // copy's origin agrees with where maven-release-plugin later pushes commits and tags.
        // GitHub reports sshUrl in scp form (git@github.com:org/repo.git) while the POMs use
        // ssh://git@github.com/org/repo - the same host and path, so git treats them alike.
        String remote = repository.getSshUrl();
        if (remote == null) {
            LOGGER.warn("No sshUrl for {}, falling back to https", repository.getName());
            remote = repository.getUrl();
        }

        desktopService.runCommand(basePath, "git", "clone", remote);
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
