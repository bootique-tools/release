package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.FileAppender;
import io.bootique.tools.release.model.maven.persistent.Project;
import io.bootique.tools.release.model.release.ReleaseDescriptor;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiAppender extends AppenderBase<ILoggingEvent>{

    private final Map<List<String>, FileAppender<ILoggingEvent>> appenderMap = new HashMap<>();
    private FileAppender<ILoggingEvent> currentAppender;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if(currentAppender != null) {
            currentAppender.doAppend(iLoggingEvent);
        }
    }

    synchronized void setCurrentAppender(List<String> args) {
        currentAppender = appenderMap.get(args);
        if(currentAppender != null && !currentAppender.isStarted()) {
            currentAppender.setContext(context);
            currentAppender.start();
        }
    }

    @Override
    public void stop() {
        if(currentAppender != null) {
            currentAppender.stop();
        }
        super.stop();
    }

    void createAppenderMap(ReleaseDescriptor releaseDescriptor, String loggerPath) {
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%date - %msg%n");
        ple.setContext(context);
        ple.start();

        for(Project project : releaseDescriptor.getProjectList()) {
            for(ReleaseStage releaseStage : ReleaseStage.values()) {
                if(releaseStage == ReleaseStage.NO_RELEASE) {
                    continue;
                }
                String logFile = loggerPath + File.separator + releaseDescriptor.getReleaseVersion() +File.separator +
                        project.getRepository().getName() + File.separator +
                        "release" + File.separator +
                        releaseStage + ".log";

                appenderMap.put(Arrays.asList(releaseDescriptor.getReleaseVersion(),
                        project.getRepository().getName(),
                        "release", String.valueOf(releaseStage)), createAppender(logFile, ple, project.getRepository().getName()));
            }
            for(RollbackStage rollbackStage : RollbackStage.values()) {
                if(rollbackStage == RollbackStage.NO_ROLLBACK) {
                    continue;
                }
                String logFile = loggerPath + File.separator + releaseDescriptor.getReleaseVersion() + File.separator +
                        project.getRepository().getName() + File.separator +
                        "rollback" + File.separator +
                        rollbackStage + ".log";
                appenderMap.put(Arrays.asList(releaseDescriptor.getReleaseVersion(),
                        project.getRepository().getName(),
                        "rollback", String.valueOf(rollbackStage)), createAppender(logFile, ple, project.getRepository().getName()));
            }
        }
    }

    Map<List<String>, FileAppender<ILoggingEvent>> getAppenderMap() {
        return appenderMap;
    }

    private FileAppender<ILoggingEvent> createAppender(String path, PatternLayoutEncoder ple, String name) {
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setAppend(false);
        fileAppender.setEncoder(ple);
        fileAppender.setFile(path);
        fileAppender.setName("file-appender-" + name);
        return fileAppender;
    }
}
