package io.bootique.tools.release.service.tasks;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.release.ReleaseService;

public class ReleaseSonatypeSyncTask implements Function<Repository, String> {

    @Inject
    LoggerService loggerService;

    @Inject
    MavenService mavenService;

    @Inject
    DesktopService desktopService;

    @Inject
    PreferenceService preferences;

    @Inject
    ReleaseService releaseService;

    @Override
    public String apply(Repository repo) {
        loggerService.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_SYNC));
        if (!mavenService.isMavenProject(repo)) {
            throw new JobException("NO_POM", "No pom.xml for repo " + repo);
        }
        Path repoPath = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName());

        List<StagingRepo> repos = getStagingRepos(repoPath);
        if(repos.isEmpty()) {
            throw new JobException("NO_STAGING_REPO", "Staging repos not found, check release perform stage logs.");
        }
        if(repos.size() > 1) {
            throw new JobException("MULTI_STAGING_REPO",
                    "Multiple staging repos found, can't automatically deal with them. "
                    + "Please go to https://oss.sonatype.com and check them manually.");
        }

        StagingRepo stagingRepo = repos.get(0);
        if(stagingRepo.state == RepoState.UNKNOWN) {
            throw new JobException("STAGING_REPO_UNKNOWN_STATE", "Staging repo is in unknown or unsupported state." +
                    "Please go to https://oss.sonatype.com and check it manually.");
        }

        if(stagingRepo.state == RepoState.OPEN) {
            closeStagingRepo(repoPath, stagingRepo);
        }

        if(stagingRepo.state != RepoState.RELEASED) {
            releaseStagingRepo(repoPath, stagingRepo);
        }

        releaseService.saveRelease(repo);
        return "";
    }

    private void closeStagingRepo(Path localRepoPath, StagingRepo remoteRepo) {
        stagingPlugin(localRepoPath, "rc-close", "-DstagingRepositoryId=" + remoteRepo.getId());
    }

    private void releaseStagingRepo(Path localRepoPath, StagingRepo remoteRepo) {
        stagingPlugin(localRepoPath, "rc-release", "-DstagingRepositoryId=" + remoteRepo.getId());
    }

    private List<StagingRepo> getStagingRepos(Path repoPath) {
        String result = stagingPlugin(repoPath, "rc-list");
        return result.lines()
                .dropWhile(s -> !s.startsWith("[INFO] Getting list of available staging repositories..."))
                .skip(3)
                .takeWhile(s -> !s.startsWith("[INFO] ------"))
                .map(s -> s.substring("[INFO] ".length()).trim().split(" "))
                .map(StagingRepo::of)
                .collect(Collectors.toList());
    }

    private String stagingPlugin(Path repoPath, String... commands) {
        String result;
        try {
            commands[0] = "nexus-staging:" + commands[0];
            result = desktopService.runMavenCommand(repoPath, commands);
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
        return result;
    }

    private enum RepoState {
        OPEN,
        CLOSED,
        RELEASED,
        UNKNOWN;

        static RepoState of(String value) {
            try {
                return RepoState.valueOf(value);
            } catch (Exception ignore) {
                return UNKNOWN;
            }
        }
    }

    private static class StagingRepo {
        private final String id;
        private final RepoState state;
        private final String description;

        static StagingRepo of(String... data) {
            if(data.length < 3) {
                throw new IllegalArgumentException();
            }
            RepoState state = null;
            StringBuilder description = new StringBuilder();
            for(int i = 1; i < data.length; i++) {
                if(state == null) {
                    if (!data[i].isBlank()) {
                        state = RepoState.of(data[i]);
                    }
                } else {
                    description.append(data[i]).append(" ");
                }
            }

            return new StagingRepo(data[0], state, description.toString());
        }

        private StagingRepo(String id, RepoState state, String description) {
            this.id = id;
            this.state = state;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public RepoState getState() {
            return state;
        }

        public String getDescription() {
            return description;
        }
    }
}
