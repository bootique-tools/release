package io.bootique.tools.release.controller;

import com.google.inject.Inject;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.preferences.PreferenceService;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

public class BaseRequestFilter implements ContainerRequestFilter {

    @Inject
    private DesktopService desktopService;

    @Inject
    private PreferenceService preferenceService;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        String output = desktopService.runCommand(preferenceService.get(GitService.BASE_PATH_PREFERENCE), "gpg", "--list-secret-keys", "--keyid-format", "LONG");
        if(output.isEmpty() || output.contains("gpg: no ultimately trusted keys found")) {
            containerRequestContext.abortWith(Response.serverError().entity("No GPG key. Please generate.").build());
        }
    }

}
