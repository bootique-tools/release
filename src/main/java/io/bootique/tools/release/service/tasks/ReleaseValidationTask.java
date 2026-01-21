package io.bootique.tools.release.service.tasks;

import ch.qos.logback.classic.Logger;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.model.release.ReleaseStage;
import io.bootique.tools.release.service.desktop.DesktopException;
import io.bootique.tools.release.service.desktop.DesktopService;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.job.JobException;
import io.bootique.tools.release.service.logger.LoggerService;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.service.validation.ValidatePomService;
import jakarta.inject.Inject;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ReleaseValidationTask implements ReleaseTask {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ReleaseValidationTask.class);

    @Inject
    LoggerService logger;

    @Inject
    PreferenceService preferences;

    @Inject
    ValidatePomService validatePomService;

    @Inject
    DesktopService desktopService;


    @Override
    public String apply(Repository repo) {
        logger.setAppender(repo.getName(), "release", String.valueOf(ReleaseStage.RELEASE_VALIDATION));
        validate(repo);
        return install(repo);
    }

    private String install(Repository repo) {
        try {
            return desktopService.runMavenCommand(
                    preferences.get(GitService.BASE_PATH_PREFERENCE).resolve(repo.getName()), "clean", "install", "-B", "-DskipTests");
        } catch (DesktopException ex) {
            throw new JobException(ex.getMessage(), ex);
        }
    }

    private void validate(Repository repo) {
        Map<String, List<String>> failedPoms = validatePomService.validatePom(repo.getName());
        if (!failedPoms.isEmpty()) {
            failedPoms.forEach((pom, msgs)
                    -> LOGGER.error("Error in pom: " + pom + ", " + String.join(";", msgs)));
            throw new DesktopException("Pom validation error");
        }
    }
}
