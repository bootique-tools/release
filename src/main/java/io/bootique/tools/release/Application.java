package io.bootique.tools.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.cayenne.CayenneModule;
import io.bootique.command.CommandDecorator;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.MapBuilder;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.client.HttpTargets;
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
import io.bootique.tools.release.service.bintray.BintrayApi;
import io.bootique.tools.release.service.bintray.credential.BintrayCredentialFactory;
import io.bootique.tools.release.service.central.DefaultMvnCentralService;
import io.bootique.tools.release.service.central.MvnCentralService;
import io.bootique.tools.release.service.console.ConsoleReleaseService;
import io.bootique.tools.release.service.console.ConsoleRollbackService;
import io.bootique.tools.release.service.console.DefaultConsoleReleaseService;
import io.bootique.tools.release.service.console.DefaultConsoleRollbackService;
import io.bootique.tools.release.service.content.ContentService;
import io.bootique.tools.release.service.content.DefaultContentService;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.desktop.GenericDesktopService;
import io.bootique.tools.release.service.desktop.LinuxDesktopService;
import io.bootique.tools.release.service.desktop.MacOSService;
import io.bootique.tools.release.service.desktop.WindowsDesktopService;
import io.bootique.tools.release.service.git.ExternalGitService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.github.GitHubRestAPI;
import io.bootique.tools.release.service.github.GitHubRestV3API;
import io.bootique.tools.release.service.github.GraphQLGitHubApi;
import io.bootique.tools.release.service.github.GraphQLGitHubApiInvalidateCache;
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
import io.bootique.tools.release.service.readme.CreateReadmeService;
import io.bootique.tools.release.service.readme.DefaultCreateReadmeService;
import io.bootique.tools.release.service.release.DefaultReleaseService;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.service.tasks.ReleaseBintrayTask;
import io.bootique.tools.release.service.tasks.ReleaseInstallTask;
import io.bootique.tools.release.service.tasks.ReleasePreparePerformTask;
import io.bootique.tools.release.service.tasks.ReleasePullTask;
import io.bootique.tools.release.service.tasks.ReleaseSyncTask;
import io.bootique.tools.release.service.tasks.RollbackBintrayTask;
import io.bootique.tools.release.service.tasks.RollbackMvnGitTask;
import io.bootique.tools.release.service.validation.DefaultValidatePomService;
import io.bootique.tools.release.service.validation.ValidatePomService;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.function.Function;
import javax.inject.Named;
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
        binder.bind(ContentService.class).to(DefaultContentService.class).inSingletonScope();
        binder.bind(CreateReadmeService.class).to(DefaultCreateReadmeService.class).inSingletonScope();
        binder.bind(ValidatePomService.class).to(DefaultValidatePomService.class).inSingletonScope();

        JettyModule.extend(binder).useDefaultServlet();

        JerseyModule.extend(binder)
                .addFeature(JacksonFeature.class)
                .addPackage(RepoController.class.getPackage());
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_PULL, ReleasePullTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_INSTALL, ReleaseInstallTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_BINTRAY_CHECK, ReleaseBintrayTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_PREPARE_PERFORM, ReleasePreparePerformTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_SYNC, ReleaseSyncTask.class);
        setRollbackFunctionClass(binder, RollbackStage.ROLLBACK_BINTRAY, RollbackBintrayTask.class);
        setRollbackFunctionClass(binder, RollbackStage.ROLLBACK_MVN, RollbackMvnGitTask.class);
        BQCoreModule.extend(binder).addCommand(ConsoleReleaseCommand.class);
        BQCoreModule.extend(binder).addCommand(ConsoleRollbackCommand.class);

        BQCoreModule.extend(binder)
                .decorateCommand(ServerCommand.class, CommandDecorator.beforeRun(ScheduleCommand.class));

        CayenneModule.extend(binder)
                .addProject("cayenne/cayenne-project.xml");

        JobModule.extend(binder).addJob(GitHubDataImportJob.class);
    }

    private static MapBuilder<ReleaseStage, Function<Repository, String>> contributeReleaseFunctionClass(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<>() {};
        TypeLiteral<ReleaseStage> key = new TypeLiteral<>() {};
        return binder.bindMap(key, type);
    }

    private static MapBuilder<RollbackStage, Function<Repository, String>> contributeRollbackFunctionClass(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<>() {
        };
        TypeLiteral<RollbackStage> key = new TypeLiteral<>() {};
        return binder.bindMap(key, type);
    }

    private static void setReleaseFunctionClass(Binder binder, ReleaseStage releaseStage,  Class<? extends Function<Repository, String>> functionClass) {
        contributeReleaseFunctionClass(binder).put(releaseStage, functionClass);
    }

    private static void setRollbackFunctionClass(Binder binder, RollbackStage rollbackStage, Class<? extends Function<Repository, String>> functionClass) {
        contributeRollbackFunctionClass(binder).put(rollbackStage, functionClass);
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
    public BintrayApi createBintrayApi(ConfigurationFactory configurationFactory,
                                       HttpTargets httpTargets,
                                       ObjectMapper objectMapper, PreferenceService preferenceService) {
        return configurationFactory
                .config(BintrayCredentialFactory.class, "maven-central")
                .createBintrayApi(httpTargets, objectMapper, preferenceService);
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
    GitHubApi provideGitHubApi(PreferenceService preferenceService, ContentService contentService) {
        return new GraphQLGitHubApi(preferenceService, contentService);
    }

    @Provides
    @Singleton
    @Named("updateCache")
    GitHubApi provideGitGubApiInvalidateCache(GraphQLService graphQLService, PreferenceService preferenceService, ContentService contentService) {
        return new GraphQLGitHubApiInvalidateCache(graphQLService, preferenceService, contentService);
    }

}
