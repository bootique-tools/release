package io.bootique.tools.release;

import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.cayenne.v42.CayenneModule;
import io.bootique.command.CommandDecorator;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.MapBuilder;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.command.ServerCommand;
import io.bootique.jetty.websocket.JettyWebSocketModule;
import io.bootique.job.command.ScheduleCommand;
import io.bootique.job.runtime.JobModule;
import io.bootique.tools.release.controller.RepoController;
import io.bootique.tools.release.controller.websocket.ReleaseWebSocket;
import io.bootique.tools.release.job.GitHubDataImportJob;
import io.bootique.tools.release.job.MavenProjectsImport;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.central.DefaultMvnCentralService;
import io.bootique.tools.release.service.central.MvnCentralService;
import io.bootique.tools.release.service.desktop.*;
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
import io.bootique.tools.release.service.logger.*;
import io.bootique.tools.release.service.maven.DefaultMavenService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.preferences.credential.PreferenceCredentialFactory;
import io.bootique.tools.release.service.readme.DefaultReleaseNotesService;
import io.bootique.tools.release.service.readme.ReleaseNotesService;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorServiceImpl;
import io.bootique.tools.release.service.release.descriptors.release.ReleaseDescriptorService;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorServiceImpl;
import io.bootique.tools.release.service.release.descriptors.repository.RepositoryDescriptorService;
import io.bootique.tools.release.service.release.executor.*;
import io.bootique.tools.release.service.release.executor.factory.JobDescriptorFactory;
import io.bootique.tools.release.service.release.executor.factory.JobDescriptorFactoryImpl;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentServiceImpl;
import io.bootique.tools.release.service.release.persistent.ReleasePersistentService;
import io.bootique.tools.release.service.release.stage.manager.StageManagerImplService;
import io.bootique.tools.release.service.release.stage.manager.StageManagerService;
import io.bootique.tools.release.service.release.stage.updater.StageListener;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterImpService;
import io.bootique.tools.release.service.release.stage.updater.StageUpdaterService;
import io.bootique.tools.release.service.tasks.*;
import io.bootique.tools.release.service.validation.DefaultValidatePomService;
import io.bootique.tools.release.service.validation.ValidatePomService;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.inject.Singleton;
import java.util.function.Function;

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
        binder.bind(MvnCentralService.class).to(DefaultMvnCentralService.class).inSingletonScope();
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
                .addEndpoint(ReleaseWebSocket.class);

        JettyModule.extend(binder)
                .useDefaultServlet();

        JerseyModule.extend(binder)
                .addFeature(JacksonFeature.class)
                .addPackage(RepoController.class.getPackage());

        contributeReleaseTask(binder)
                .put(ReleaseStage.RELEASE_PULL, ReleasePullTask.class)
                .put(ReleaseStage.RELEASE_INSTALL, ReleaseInstallTask.class)
                .put(ReleaseStage.RELEASE_PREPARE, ReleasePrepareTask.class)
                .put(ReleaseStage.RELEASE_PERFORM, ReleasePerformTask.class)
                .put(ReleaseStage.RELEASE_SYNC, ReleaseSonatypeSyncTask.class);


        contributeRollbackTask(binder)
                .put(RollbackStage.ROLLBACK_SONATYPE, RollbackSonatypeTask.class)
                .put(RollbackStage.ROLLBACK_MVN, RollbackMvnGitTask.class);

        BQCoreModule.extend(binder)
                .decorateCommand(ServerCommand.class, CommandDecorator.beforeRun(ScheduleCommand.class));

        CayenneModule.extend(binder)
                .addProject("cayenne/cayenne-project.xml");

        JobModule.extend(binder)
                .addJob(GitHubDataImportJob.class)
                .addJob(MavenProjectsImport.class);
    }

    private static MapBuilder<ReleaseStage, Function<Repository, String>> contributeReleaseTask(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<>() {
        };
        TypeLiteral<ReleaseStage> key = new TypeLiteral<>() {
        };
        return binder.bindMap(key, type);
    }

    private static MapBuilder<RollbackStage, Function<Repository, String>> contributeRollbackTask(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<>() {
        };
        TypeLiteral<RollbackStage> key = new TypeLiteral<>() {
        };
        return binder.bindMap(key, type);
    }

    @Provides
    @Singleton
    DesktopService createDesktopService() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new WindowsDesktopService();
        }
        if (os.contains("mac")) {
            return new MacOSService();
        }
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return new LinuxDesktopService();
        }
        return new GenericDesktopService();
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
