package io.bootique.tools.release.model.session;

/**
 * Data the UI shell needs on every page: who we are, which organization we manage, and the nav badge counts.
 * <p>
 * {@code organization} and {@code user} are null until the first GitHub import completes, and the UI is expected
 * to render a "no data from GitHub API" hint in that case rather than treat it as an error.
 */
public record SessionResponse(OrganizationInfo organization, UserInfo user, Counts counts) {

    public record OrganizationInfo(String login, String name, String url) {
    }

    public record UserInfo(String login, String name, String url) {
    }

    public record Counts(int repositories, int issues, int pullRequests) {
    }
}
