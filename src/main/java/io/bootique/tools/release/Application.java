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
import io.bootique.job.command.ScheduleCommand;
import io.bootique.job.runtime.JobModule;
import io.bootique.tools.release.command.ConsoleReleaseCommand;
import io.bootique.tools.release.command.ConsoleRollbackCommand;
import io.bootique.tools.release.controller.RepoController;
import io.bootique.tools.release.job.GitHubDataImportJob;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.model.release.RollbackStage;
import io.bootique.tools.release.service.central.DefaultMvnCentralService;
import io.bootique.tools.release.service.central.MvnCentralService;
import io.bootique.tools.release.service.console.ConsoleReleaseService;
import io.bootique.tools.release.service.console.ConsoleRollbackService;
import io.bootique.tools.release.service.console.DefaultConsoleReleaseService;
import io.bootique.tools.release.service.console.DefaultConsoleRollbackService;
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
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.maven.DefaultMavenService;
import io.bootique.tools.release.service.maven.MavenService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.preferences.credential.PreferenceCredentialFactory;
import io.bootique.tools.release.service.readme.DefaultReleaseNotesService;
import io.bootique.tools.release.service.readme.ReleaseNotesService;
import io.bootique.tools.release.service.release.DefaultReleaseService;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.service.tasks.ReleaseInstallTask;
import io.bootique.tools.release.service.tasks.ReleasePreparePerformTask;
import io.bootique.tools.release.service.tasks.ReleasePullTask;
import io.bootique.tools.release.service.tasks.ReleaseSonatypeSyncTask;
import io.bootique.tools.release.service.tasks.RollbackMvnGitTask;
import io.bootique.tools.release.service.tasks.RollbackSonatypeTask;
import io.bootique.tools.release.service.validation.DefaultValidatePomService;
import io.bootique.tools.release.service.validation.ValidatePomService;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.function.Function;
import javax.inject.Singleton;

//--config="release-manager.yml" --server
public class Application implements BQModule  {
    public static void main(String[] args) {
        Bootique
                .app(args)
                .autoLoadModules()
                .module(Application.class)
                .args("--config=classpath:settings.yml")
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
        binder.bind(ReleaseService.class).to(DefaultReleaseService.class).inSingletonScope();
        binder.bind(LoggerService.class).to(DefaultLoggerService.class).inSingletonScope();
        binder.bind(ConsoleReleaseService.class).to(DefaultConsoleReleaseService.class).inSingletonScope();
        binder.bind(ConsoleRollbackService.class).to(DefaultConsoleRollbackService.class).inSingletonScope();
        binder.bind(MvnCentralService.class).to(DefaultMvnCentralService.class).inSingletonScope();
        binder.bind(ReleaseNotesService.class).to(DefaultReleaseNotesService.class).inSingletonScope();
        binder.bind(ValidatePomService.class).to(DefaultValidatePomService.class).inSingletonScope();

        JettyModule.extend(binder)
                .useDefaultServlet();

        JerseyModule.extend(binder)
                .addFeature(JacksonFeature.class)
                .addPackage(RepoController.class.getPackage());

        contributeReleaseTask(binder)
                .put(ReleaseStage.RELEASE_PULL,            ReleasePullTask.class)
                .put(ReleaseStage.RELEASE_INSTALL,         ReleaseInstallTask.class)
                .put(ReleaseStage.RELEASE_PREPARE_PERFORM, ReleasePreparePerformTask.class)
                .put(ReleaseStage.RELEASE_SYNC,            ReleaseSonatypeSyncTask.class);

        contributeRollbackTask(binder)
                .put(RollbackStage.ROLLBACK_SONATYPE,     RollbackSonatypeTask.class)
                .put(RollbackStage.ROLLBACK_MVN,          RollbackMvnGitTask.class);

        BQCoreModule.extend(binder)
                .addCommand(ConsoleReleaseCommand.class)
                .addCommand(ConsoleRollbackCommand.class)
                .decorateCommand(ServerCommand.class, CommandDecorator.beforeRun(ScheduleCommand.class));

        CayenneModule.extend(binder)
                .addProject("cayenne/cayenne-project.xml");

        JobModule.extend(binder)
                .addJob(GitHubDataImportJob.class);
    }

    private static MapBuilder<ReleaseStage, Function<Repository, String>> contributeReleaseTask(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<>() {};
        TypeLiteral<ReleaseStage> key = new TypeLiteral<>() {};
        return binder.bindMap(key, type);
    }

    private static MapBuilder<RollbackStage, Function<Repository, String>> contributeRollbackTask(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<>() {
        };
        TypeLiteral<RollbackStage> key = new TypeLiteral<>() {};
        return binder.bindMap(key, type);
    }

    @Provides
    @Singleton
    DesktopService createDesktopService() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            return new WindowsDesktopService();
        }
        if(os.contains("mac")) {
            return new MacOSService();
        }
        if(os.contains("nix") || os.contains("nux") || os.contains("aix")) {
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
