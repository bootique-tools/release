package io.bootique.tools.release.service.content;

import io.bootique.tools.release.model.persistent.Issue;
import io.bootique.tools.release.model.persistent.Milestone;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.PullRequest;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.util.RequestCache;

import javax.ws.rs.core.Configuration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface ContentService {

    Map<String, RequestCache<?>> getRepoCache();

    List<Issue> getIssues(Organization organization, List<Predicate<Issue>> predicates, Comparator<Issue> comparator);

    List<Milestone> getMilestones(Organization organization);

    List<PullRequest> getPullRequests(Organization organization, Predicate<PullRequest> predicate, Comparator<PullRequest> comparator);

    List<Repository> getRepositories(Organization organization, Predicate<Repository> predicate, Comparator<Repository> comparator);

    Repository getRepository(String organizationName, String name);

    boolean haveCache(Configuration configuration);
}
