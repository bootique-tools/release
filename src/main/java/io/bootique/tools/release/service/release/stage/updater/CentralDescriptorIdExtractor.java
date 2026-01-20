package io.bootique.tools.release.service.release.stage.updater;

import io.bootique.tools.release.model.release.RepositoryDescriptor;

import java.util.Arrays;

public class CentralDescriptorIdExtractor implements OutProcessor {

    @Override
    public void accept(RepositoryDescriptor repositoryDescriptor, String out) {
        if(out == null) {
            return;
        }

        // [INFO] [INFO] Deployment d5dc2885-cd89-4a06-b873-26057232bb1b has been validated. To finish publishing visit https://central.sonatype.com/publishing/deployments
        String prefix = "[INFO] [INFO] Deployment ";
        Arrays.stream(out.split("\n"))
                .filter(s -> s.startsWith(prefix))
                .findFirst()
                .ifPresent(s -> {
                    repositoryDescriptor.setCentralDeploymentId(s.substring(prefix.length(), s.indexOf(" has been validated")));
                });
    }
}
