package io.bootique.tools.release.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.tools.release.service.github.GitHubApi;

public class GithubInit implements Command {

    @Inject
    private Provider<GitHubApi> provider;

    @Override
    public CommandOutcome run(Cli cli) {
        provider.get().getCurrentUser();
        provider.get().getCurrentOrganization();
        return CommandOutcome.succeeded();
    }
}
