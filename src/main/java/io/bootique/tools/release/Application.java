package io.bootique.tools.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.command.CommandDecorator;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.client.HttpTargets;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.command.ServerCommand;
import io.bootique.tools.release.command.ConsoleReleaseCommand;
import io.bootique.tools.release.command.ConsoleRollbackCommand;
import io.bootique.tools.release.command.GithubInit;
import io.bootique.tools.release.controller.RepoController;
import io.bootique.tools.release.model.github.Repository;
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
import io.bootique.tools.release.service.release.DefaultReleaseService;
import io.bootique.tools.release.service.release.ReleaseService;
import io.bootique.tools.release.service.tasks.ReleaseBintrayTask;
import io.bootique.tools.release.service.tasks.ReleaseInstallTask;
import io.bootique.tools.release.service.tasks.ReleasePreparePerformTask;
import io.bootique.tools.release.service.tasks.ReleasePullTask;
import io.bootique.tools.release.service.tasks.ReleaseSyncTask;
import io.bootique.tools.release.service.tasks.RollbackBintrayTask;
import io.bootique.tools.release.service.tasks.RollbackMvnGitTask;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.function.Function;

public class Application implements Module {
    public static void main(String[] args) {
        Bootique
                .app(args)
                .autoLoadModules()
                .module(Application.class)
//                .args("--server")
//                .args("--release",
//                        "--fromVersion=0.26-SNAPSHOT",
//                        "--releaseVersion=0.26",
//                        "--devVersion=0.27-SNAPSHOT")
//                .args("--rollback")
                .args("--config=classpath:settings.yml")
                .exec()
                .exit();
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(GraphQLService.class).to(SimpleGraphQLService.class).in(Singleton.class);
        binder.bind(GitHubApi.class).to(GraphQLGitHubApi.class).in(Singleton.class);
        binder.bind(GitHubRestAPI.class).to(GitHubRestV3API.class).in(Singleton.class);
        binder.bind(GitService.class).to(ExternalGitService.class).in(Singleton.class);
        binder.bind(MavenService.class).to(DefaultMavenService.class).in(Singleton.class);
        binder.bind(BatchJobService.class).to(DefaultBatchJobService.class).in(Singleton.class);
        binder.bind(ReleaseService.class).to(DefaultReleaseService.class).in(Singleton.class);
        binder.bind(LoggerService.class).to(DefaultLoggerService.class).in(Singleton.class);
        binder.bind(ConsoleReleaseService.class).to(DefaultConsoleReleaseService.class).in(Singleton.class);
        binder.bind(ConsoleRollbackService.class).to(DefaultConsoleRollbackService.class).in(Singleton.class);
        binder.bind(MvnCentralService.class).to(DefaultMvnCentralService.class).in(Singleton.class);
        JettyModule.extend(binder).useDefaultServlet();
        JerseyModule.extend(binder)
                .addFeature(JacksonFeature.class)
                .addPackage(RepoController.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_PULL, ReleasePullTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_INSTALL, ReleaseInstallTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_BINTRAY_CHECK, ReleaseBintrayTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_PREPARE_PERFORM, ReleasePreparePerformTask.class);
        setReleaseFunctionClass(binder, ReleaseStage.RELEASE_SYNC, ReleaseSyncTask.class);
        setRollbackFunctionClass(binder, RollbackStage.ROLLBACK_BINTRAY, RollbackBintrayTask.class);
        setRollbackFunctionClass(binder, RollbackStage.ROLLBACK_MVN, RollbackMvnGitTask.class);
        BQCoreModule.extend(binder).addCommand(ConsoleReleaseCommand.class);
        BQCoreModule.extend(binder).addCommand(ConsoleRollbackCommand.class);
        BQCoreModule.extend(binder).addCommand(GithubInit.class);

        BQCoreModule.extend(binder)
                .decorateCommand(ServerCommand.class, CommandDecorator.beforeRun(GithubInit.class));
    }


    private static MapBinder<ReleaseStage, Function<Repository, String>> contributeReleaseFunctionClass(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<Function<Repository, String>>(){};
        TypeLiteral<ReleaseStage> key = new TypeLiteral<ReleaseStage>(){};
        return MapBinder.newMapBinder(binder, key, type);
    }

    private static MapBinder<RollbackStage, Function<Repository, String>> contributeRollbackFunctionClass(Binder binder) {
        TypeLiteral<Function<Repository, String>> type = new TypeLiteral<Function<Repository, String>>(){};
        TypeLiteral<RollbackStage> key = new TypeLiteral<RollbackStage>(){};
        return MapBinder.newMapBinder(binder, key, type);
    }

    private static void setReleaseFunctionClass(Binder binder, ReleaseStage releaseStage,  Class<? extends Function<Repository, String>> functionClass) {
        contributeReleaseFunctionClass(binder).addBinding(releaseStage).to(functionClass);
    }

    private static void setRollbackFunctionClass(Binder binder, RollbackStage rollbackStage, Class<? extends Function<Repository, String>> functionClass) {
        contributeRollbackFunctionClass(binder).addBinding(rollbackStage).to(functionClass);
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
    public BintrayApi createBintrayApi(ConfigurationFactory configurationFactory, HttpTargets httpTargets, ObjectMapper objectMapper, PreferenceService preferenceService) {
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

}
