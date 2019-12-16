package io.bootique.tools.release.controller;

import java.nio.file.Paths;

import io.bootique.tools.release.service.desktop.DesktopService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

public class BaseRequestFilter implements ContainerRequestFilter {

    @Inject
    private DesktopService desktopService;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        String output = desktopService.runCommand(Paths.get(System.getProperty("user.home")), "gpg", "--list-secret-keys", "--keyid-format", "LONG");
        if(output.isEmpty() || output.contains("gpg: no ultimately trusted keys found")) {
            containerRequestContext.abortWith(Response.serverError().entity("No GPG key. Please generate.").build());
        }
    }

}
