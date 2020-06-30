package io.bootique.tools.release.command;

import javax.inject.Inject;
import javax.inject.Provider;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.tools.release.service.console.ConsoleRollbackService;
import io.bootique.tools.release.service.job.JobException;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;

public class ConsoleRollbackCommand extends CommandWithMetadata {

    @Inject
    private Provider<ConsoleRollbackService> provider;

//    private Provider<ServerRuntime> runtimeProvider;


    public ConsoleRollbackCommand() {
        super(createMetadata());
    }
//
//    @Inject
//    public ConsoleRollbackCommand(Provider<ServerRuntime> runtimeProvider) {
//        super(createMetadata());
//        this.runtimeProvider = runtimeProvider;
//    }

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(ConsoleReleaseCommand.class)
                .name("rollback")
                .description("Launch rollback from console.")
                .build();
    }

    @Override
    public CommandOutcome run(Cli cli) {

//        ServerRuntime runtime = runtimeProvider.get();
//
//        ObjectContext context = runtime.newContext();

        if(!provider.get().checkReadyForRollback()) {
            return CommandOutcome.failed(-1, "Command failed");
        }

        try {
            provider.get().startRollbackFromConsole();
        }
        catch (JobException ex) {
            return CommandOutcome.failed(1,  "Rollback was stopped because of ", ex);
        }
        return CommandOutcome.succeeded();
    }
}
