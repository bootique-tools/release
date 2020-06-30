package io.bootique.tools.release.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.tools.release.service.console.ConsoleReleaseService;
import io.bootique.tools.release.service.desktop.DesktopException;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

public class ConsoleReleaseCommand extends CommandWithMetadata{

    @Inject
    private Provider<ConsoleReleaseService> provider;

    public ConsoleReleaseCommand() {
        super(createMetadata());
    }

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(ConsoleReleaseCommand.class)
                .name("release")
                .shortName('r')
                .description("Launch release from console.")
                .addOption(OptionMetadata
                        .builder("fromVersion", "Set version to release")
                        .valueRequired()
                        .build())
                .addOption(OptionMetadata
                        .builder("releaseVersion", "Set the release version")
                        .valueRequired()
                        .build())
                .addOption(OptionMetadata
                        .builder("devVersion", "Set next dev version")
                        .valueRequired()
                        .build())
                .addOption(OptionMetadata
                        .builder("excludeModule", "Exclude module from release")
                        .valueRequired()
                        .build())
                .build();
    }

    @Override
    public CommandOutcome run(Cli cli) {
        String fromVersion = cli.optionString("fromVersion");
        String releaseVersion = cli.optionString("releaseVersion");
        String devVersion = cli.optionString("devVersion");
        List<String> excludeModules = cli.optionStrings("excludeModule");

        if(!provider.get().checkReadyForRelease(fromVersion, releaseVersion, devVersion, excludeModules)) {
            return CommandOutcome.failed(-1, "Command failed");
        }

        try {
            provider.get().startReleaseFromConsole();
        } catch (DesktopException ex) {
            return CommandOutcome.failed(1,  "Release was stopped because of ", ex);
        }

        return CommandOutcome.succeeded();
    }
}
