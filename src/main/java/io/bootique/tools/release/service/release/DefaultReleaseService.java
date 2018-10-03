package io.bootique.tools.release.service.release;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.*;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultReleaseService implements ReleaseService{

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DefaultReleaseService.class);

    @Inject
    PreferenceService preferences;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    BatchJobService jobService;

    @Inject
    GitHubApi gitHubApi;

    @Inject
    Map<ReleaseStage, Function<Repository, String>> releaseMap;

    @Inject
    Map<RollbackStage, Function<Repository, String>> rollbackMap;

    private ReleaseDescriptor releaseDescriptor;

    public DefaultReleaseService() {
    }

    @Override
    public void createReleaseDescriptor(ReleaseDescriptor releaseDescriptor) {
       this.releaseDescriptor = releaseDescriptor;
    }

    @Override
    public ReleaseDescriptor getReleaseDescriptor() {
        return releaseDescriptor;
    }

    @Override
    public void rollbackPom(String repoName, ReleaseDescriptor releaseDescriptor) {
        LOGGER.debug("Rollback from " + releaseDescriptor.getDevVersion() + " to " + releaseDescriptor.getFromVersion());
        Path path = preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repoName);
        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(name -> (name.getFileName().toString().equals("pom.xml") && !name.toString().contains(File.pathSeparator + "target" + File.pathSeparator)))
                    .forEach(filePath -> {
                        try (Stream<String> lines = Files.lines(filePath)) {
                            List<String> replaced = lines
                                    .map(line -> line.replaceAll("<version>" + releaseDescriptor.getDevVersion() + "</version>", "<version>" + releaseDescriptor.getFromVersion() + "</version>"))
                                    .collect(Collectors.toList());
                            replaced.forEach(line -> {
                                if (line.contains("<tag>" + releaseDescriptor.getReleaseVersion() + "</tag>")) {
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

    @Override
    public void saveRelease() {
        if(!preferences.have(ReleaseService.SAVE_PATH)) {
            throw new DesktopException("Can't save release.");
        }
        Path path = Paths.get(preferences.get(ReleaseService.SAVE_PATH), releaseDescriptor.getReleaseVersion());
        Path lockPath = Paths.get(preferences.get(ReleaseService.SAVE_PATH), "lock.txt");
        Path pathFile = path.resolve(releaseDescriptor.getReleaseVersion() + ".txt");
        try {
            if(!Files.exists(path)) {
                Files.createDirectories(path);
            }
            if(!Files.exists(lockPath)) {
                Files.createFile(lockPath);
            }
            Files.write(lockPath, releaseDescriptor.getReleaseVersion().getBytes());
            if(!Files.exists(pathFile)) {
                Files.createFile(pathFile);
            }
            String json = objectMapper.writeValueAsString(releaseDescriptor);
            Files.write(pathFile, json.getBytes());
        }  catch (IOException e) {
            throw new DesktopException("Can't save release. ", e);
        }
    }

    @Override
    public void deleteLock() {
        Path pathLock = Paths.get(preferences.get(ReleaseService.SAVE_PATH), "lock.txt");
        if(Files.exists(pathLock)) {
            try {
                Files.delete(pathLock);
            } catch (IOException e) {
                throw new DesktopException("Can't delete lock file. ", e);
            }
        }
    }

    @Override
    public boolean hasCurrentActiveRelease() {
        Path pathLock = Paths.get(preferences.get(ReleaseService.SAVE_PATH), "lock.txt");
        if(Files.exists(pathLock)){
            try {
                List<String> lockList = Files.readAllLines(pathLock);
                if(lockList.size() != 1){
                    throw new DesktopException("Can't load last release.");
                }
                Path path = Paths.get(preferences.get(ReleaseService.SAVE_PATH), lockList.get(0), lockList.get(0) + ".txt");
                byte[] jsonDescriptor = Files.readAllBytes(path);
                releaseDescriptor = objectMapper.readValue(jsonDescriptor, ReleaseDescriptor.class);
                releaseDescriptor.getProjectList().forEach(project -> project.getRepository().setOrganization(gitHubApi.getCurrentOrganization()));
                return true;
            } catch (IOException e) {
                throw new DesktopException("Can't load last release. ", e);
            }
        }
        return false;
    }

    @Override
    public void nextReleaseStage() {
        if(!preferences.have(BatchJobService.CURRENT_JOB_ID)) {
            startJob(releaseMap.get(releaseDescriptor.getCurrentReleaseStage()));
        } else {
            long jobId = preferences.get(BatchJobService.CURRENT_JOB_ID);
            BatchJob<Repository, String> job = jobService.getJobById(jobId);
            if (job.isDone()) {
                releaseDescriptor.setCurrentReleaseStage(ReleaseStage.getNext(releaseDescriptor.getCurrentReleaseStage()));
                startJob(releaseMap.get(releaseDescriptor.getCurrentReleaseStage()));
            }
        }
    }

    @Override
    public void nextRollbackStage() {
        if(!preferences.have(BatchJobService.CURRENT_JOB_ID)) {
            startJob(rollbackMap.get(releaseDescriptor.getCurrentRollbackStage()));
        } else {
            long jobId = preferences.get(BatchJobService.CURRENT_JOB_ID);
            BatchJob<Repository, String> job = jobService.getJobById(jobId);
            if(job.isDone()) {
                releaseDescriptor.setCurrentRollbackStage(RollbackStage.getNext(releaseDescriptor.getCurrentRollbackStage()));
                startJob(rollbackMap.get(releaseDescriptor.getCurrentRollbackStage()));
            }
        }
    }

    @Override
    public void createThreadForRelease() {
        Runnable myRunnable = () -> {
            do {
                if(preferences.have(BatchJobService.CURRENT_JOB_ID)) {
                    BatchJob<Repository, String> job = jobService.getJobById(preferences.get(BatchJobService.CURRENT_JOB_ID));
                    if (job != null) {
                        if (releaseDescriptor.isAutoReleaseMode() && job.isDone() && !hasFailures(job)) {
                            nextReleaseStage();
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new DesktopException("Problems with auto release. ", e);
                }
            } while(releaseDescriptor.getCurrentReleaseStage() != ReleaseStage.RELEASE_SYNC);
        };
        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    @Override
    public void prepareRelease() {
        preferences.reset(BatchJobService.CURRENT_JOB_ID);
    }

    private void startJob(Function<Repository, String> proc) {
        List<Repository> repositories = new ArrayList<>();
        for (Project project : releaseDescriptor.getProjectList()) {
            repositories.add(project.getRepository());
        }
        BatchJobDescriptor<Repository, String> descriptor = new BatchJobDescriptor<>(repositories,
                proc,
                ErrorPolicy.ABORT_ALL_ON_ERROR);
        preferences.set(BatchJobService.CURRENT_JOB_ID, jobService.submit(descriptor).getId());
    }

    private boolean hasFailures(BatchJob<Repository, String> job) {
        for(BatchJobResult<Repository, String> jobResult : job.getResults()) {
            if(jobResult.getStatus() == BatchJobStatus.FAILURE){
                return true;
            }
        }
        return false;
    }
}
