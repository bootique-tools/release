package io.bootique.tools.release.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Serves the single-page app shell for every {@code /ui/**} browser path, so that client-side routes survive a
 * reload or a bookmark, and points the bare context root at the app entry.
 * <p>
 * Requests for the REST API under {@code /api/}, for WebSocket endpoints, and for real files (anything with a
 * file extension, including the app's own hashed assets) pass straight through to the static servlet.
 */
public class SpaFilter extends HttpFilter {

    static final String SPA_ROOT = "/ui";
    static final String SPA_HOME = "/ui/";
    static final String SPA_SHELL = "/ui/index.html";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.isEmpty() || "/".equals(path) || SPA_ROOT.equals(path)) {
            res.sendRedirect(req.getContextPath() + SPA_HOME);
            return;
        }

        if (path.startsWith(SPA_HOME) && !looksLikeFile(path)) {
            req.getRequestDispatcher(SPA_SHELL).forward(req, res);
            return;
        }

        chain.doFilter(req, res);
    }

    /**
     * A path segment carrying an extension is a real resource; a client-side route never is.
     */
    private static boolean looksLikeFile(String path) {
        return path.lastIndexOf('.') > path.lastIndexOf('/');
    }
}
