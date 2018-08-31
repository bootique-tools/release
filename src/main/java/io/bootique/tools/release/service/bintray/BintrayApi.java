package io.bootique.tools.release.service.bintray;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.maven.Project;
import io.bootique.tools.release.service.preferences.Preference;

import javax.ws.rs.core.Response;
import java.util.Map;

public interface BintrayApi {

    Preference<String> BINTRAY_ORG_NAME = Preference.of("bintray.org.name", String.class);
    
    void publishUploadedContent(Repository repository, String releaseVersion);

    void syncWithCentral(Repository repository, String releaseVersion);

    Response getRepository(Repository repository);

    void createRepository(Repository repository);

    void getAndDeleteVersion(Repository repository, String releaseVersion);

    Map<String, String> getPackageInfo(Project project);
}
