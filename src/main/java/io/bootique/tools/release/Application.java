package io.bootique.tools.release;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.Bootique;
import io.bootique.cayenne.v42.CayenneModule;
import io.bootique.command.CommandDecorator;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedServlet;
import io.bootique.jetty.command.ServerCommand;
import io.bootique.jetty.websocket.JettyWebSocketModule;
import io.bootique.job.JobsModule;
import io.bootique.job.command.ScheduleCommand;
import io.bootique.tools.release.controller.RepoController;
import io.bootique.tools.release.controller.websocket.JobStatusWebSocket;
import io.bootique.tools.release.controller.websocket.ReleaseWebSocket;
import io.bootique.tools.release.job.GitHubDataImportJob;
import io.bootique.tools.release.job.MavenProjectsImport;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.desktop.GenericDesktopService;
import io.bootique.tools.release.service.desktop.LinuxDesktopService;
import io.bootique.tools.release.service.desktop.MacOSService;
import io.bootique.tools.release.service.desktop.WindowsDesktopService;
import io.bootique.tools.release.service.git.ExternalGitService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApiImport;
import io.bootique.tools.release.service.github.GitHubRestAPI;
import io.bootique.tools.release.service.github.GitHubRestV3API;
import io.bootique.tools.release.service.github.GraphQLGitHubApiImport;
import io.bootique.tools.release.service.graphql.GraphQLService;
import io.bootique.tools.release.service.graphql.SimpleGraphQLService;
import io.bootique.tools.release.service.job.BatchJobService;
import io.bootique.tools.release.service.job.DefaultBatchJobService;
import io.bootique.tools.release.service.logger.DefaultLoggerService;
import io.bootique.tools.release.service.logger.ExecutionLogger;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.logger.ReleaseExecutionLogger;
import io.bootique.tools.release.service.logger.RollbackExecutionLogger;
import io.bootique.tools.release.service.maven.DefaultMavenService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.preferences.credential.PreferenceCredentialFactory;
import io.bootique.tools.release.service.readme.DefaultReleaseNotesService;
import io.bootique.tools.release.service.readme.ReleaseNotesService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorService;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorServiceImpl;
import io.bootique.tools.release.service.release.executor.ReleaseExecutor;
import io.bootique.tools.release.service.release.executor.ReleaseExecutorService;
import io.bootique.tools.release.service.release.executor.RollbackExecutor;
import io.bootique.tools.release.service.release.executor.RollbackExecutorService;
import io.bootique.tools.release.service.release.executor.factory.JobDescriptorFactory;
import io.bootique.tools.release.service.release.executor.factory.JobDescriptorFactoryImpl;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentServiceImpl;
import io.bootique.tools.release.service.release.stage.manager.StageManagerImplService;
import io.bootique.tools.release.service.release.stage.manager.StageManagerService;
import io.bootique.tools.release.service.release.stage.updater.StageListener;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterImpService;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterService;
import io.bootique.tools.release.service.tasks.ReleaseCentralPublishTask;
import io.bootique.tools.release.service.tasks.ReleasePerformTask;
import io.bootique.tools.release.service.tasks.ReleasePrepareTask;
import io.bootique.tools.release.service.tasks.ReleasePullTask;
import io.bootique.tools.release.service.tasks.ReleaseTask;
import io.bootique.tools.release.service.tasks.ReleaseValidationTask;
import io.bootique.tools.release.service.tasks.RollbackMvnGitTask;
import io.bootique.tools.release.service.tasks.RollbackSonatypeTask;
import io.bootique.tools.release.service.validation.DefaultValidatePomService;
import io.bootique.tools.release.service.validation.ValidatePomService;
import jakarta.inject.Singleton;
import org.glassfish.jersey.jackson.JacksonFeature;

//--config="release-manager.yml" --server
public class Application implements BQModule {
    public static void main(String[] args) {
        Bootique
                .app(args)
                .autoLoadModules()
                .module(Application.class)
                .args("--config=classpath:settings.yml", "--server", "--config=release-manager.yml")
                .exec()
                .exit();
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(GraphQLService.class).to(SimpleGraphQLService.class).inSingletonScope();
        binder.bind(GitHubRestAPI.class).to(GitHubRestV3API.class).inSingletonScope();
        binder.bind(GitService.class).to(ExternalGitService.class).inSingletonScope();
        binder.bind(MavenService.class).to(DefaultMavenService.class).inSingletonScope();
        binder.bind(BatchJobService.class).to(DefaultBatchJobService.class).inSingletonScope();
        binder.bind(LoggerService.class).to(DefaultLoggerService.class).inSingletonScope();
        binder.bind(ReleaseNotesService.class).to(DefaultReleaseNotesService.class).inSingletonScope();
        binder.bind(ValidatePomService.class).to(DefaultValidatePomService.class).inSingletonScope();

        binder.bind(ReleaseDescriptorService.class).to(ReleaseDescriptorServiceImpl.class).inSingletonScope();
        binder.bind(RepositoryDescriptorService.class).to(RepositoryDescriptorServiceImpl.class);

        binder.bind(JobDescriptorFactory.class).to(JobDescriptorFactoryImpl.class);
        binder.bind(ReleaseExecutorService.class).to(ReleaseExecutor.class);
        binder.bind(RollbackExecutorService.class).to(RollbackExecutor.class);

        binder.bind(ReleasePersistentService.class).to(ReleasePersistentServiceImpl.class);

        binder.bind(StageManagerService.class).to(StageManagerImplService.class);

        binder.bind(StageListener.class).to(StageUpdaterImpService.class).inSingletonScope();
        binder.bind(StageUpdaterService.class).to(StageUpdaterImpService.class).inSingletonScope();

        binder.bind(ExecutionLogger.class, "release").to(ReleaseExecutionLogger.class);
        binder.bind(ExecutionLogger.class, "rollback").to(RollbackExecutionLogger.class);

        JettyWebSocketModule.extend(binder)
                .addEndpoint(ReleaseWebSocket.class)
                .addEndpoint(JobStatusWebSocket.class);

        JettyModule.extend(binder)
                .addMappedServlet(MappedServlet.ofStatic("/").name("default").build());

        JerseyModule.extend(binder)
                .addFeature(JacksonFeature.class)
                .addPackage(RepoController.class.getPackage());

        binder.bindMap(ReleaseStage.class, ReleaseTask.class)
                .put(ReleaseStage.RELEASE_PULL, ReleasePullTask.class)
                .put(ReleaseStage.RELEASE_VALIDATION, ReleaseValidationTask.class)
                .put(ReleaseStage.RELEASE_PREPARE, ReleasePrepareTask.class)
                .put(ReleaseStage.RELEASE_PERFORM, ReleasePerformTask.class)
                .put(ReleaseStage.RELEASE_SYNC, ReleaseCentralPublishTask.class);


        binder.bindMap(RollbackStage.class, ReleaseTask.class)
                .put(RollbackStage.ROLLBACK_SONATYPE, RollbackSonatypeTask.class)
                .put(RollbackStage.ROLLBACK_MVN, RollbackMvnGitTask.class);

        BQCoreModule.extend(binder)
                .decorateCommand(ServerCommand.class, CommandDecorator.beforeRun(ScheduleCommand.class));

        CayenneModule.extend(binder)
                .addLocation("classpath:cayenne/cayenne-project.xml");

        JobsModule.extend(binder)
                .addJob(GitHubDataImportJob.class)
                .addJob(MavenProjectsImport.class);
    }

    @Provides
    @Singleton
    DesktopService createDesktopService(PreferenceService preferenceService) {
        String os = System.getProperty("os.name").toLowerCase();
        String javaHome = preferenceService.get(DesktopService.JAVA_HOME);
        if (os.contains("win")) {
            return new WindowsDesktopService(javaHome);
        }
        if (os.contains("mac")) {
            return new MacOSService(javaHome);
        }
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxDesktopService(javaHome);
        }
        return new GenericDesktopService(javaHome);
    }

    @Singleton
    @Provides
    public PreferenceService createPreferenceService(ConfigurationFactory configurationFactory) {
        return configurationFactory
                .config(PreferenceCredentialFactory.class, "preferences")
                .createPreferenceService();
    }

    @Provides
    @Singleton
    GitHubApiImport provideGitGubApiInvalidateCache(GraphQLService graphQLService, PreferenceService preferenceService) {
        return new GraphQLGitHubApiImport(graphQLService, preferenceService);
    }
}
