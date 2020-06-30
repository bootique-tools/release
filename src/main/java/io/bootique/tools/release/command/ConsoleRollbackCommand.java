package io.bootique.tools.release.command;

import javax.inject.Inject;
import javax.inject.Provider;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.tools.release.service.console.ConsoleRollbackService;
import io.bootique.tools.release.service.job.JobException;

public class ConsoleRollbackCommand extends CommandWithMetadata {

    @Inject
    private Provider<ConsoleRollbackService> provider;

    public ConsoleRollbackCommand() {
        super(createMetadata());
    }

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(ConsoleReleaseCommand.class)
                .name("rollback")
                .description("Launch rollback from console.")
                .build();
    }

    @Override
    public CommandOutcome run(Cli cli) {

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
