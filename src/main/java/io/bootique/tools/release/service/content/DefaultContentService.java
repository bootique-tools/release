package io.bootique.tools.release.service.content;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.service.git.GitService;
import io.bootique.tools.release.service.github.GitHubApi;
import io.bootique.tools.release.service.preferences.PreferenceService;
import io.bootique.tools.release.util.RequestCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.ws.rs.core.Configuration;

public class DefaultContentService implements ContentService {

    @Inject
    private GitHubApi gitHubApi;

    @Inject
    private GitService gitService;

    @Inject
    private PreferenceService preferenceService;

    private final Map<String, RequestCache<?>> repoCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, RequestCache<?>> getRepoCache() {
        return repoCache;
    }

    @Override
    public boolean haveCache(Configuration configuration) {
        AgRequest agRequest = Ag.request(configuration).build();
        DataResponse<Organization> organizations = Ag.select(Organization.class, configuration).request(agRequest).get();
        if (organizations.getObjects().size() == 0 ||
                organizations.getObjects().get(0).getRepositories().size() == 0) {
            return false;
        }
        return true;
    }
}
