package io.bootique.tools.release.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SpaFilterTest {

    private SpaFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        filter = new SpaFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher(any())).thenReturn(dispatcher);
    }

    private void doFilter(String uri) throws Exception {
        when(request.getRequestURI()).thenReturn(uri);
        filter.doFilter(request, response, chain);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/ui"})
    public void redirectsToSpaHome(String uri) throws Exception {
        doFilter(uri);

        verify(response).sendRedirect("/ui/");
        verifyNoInteractions(chain, dispatcher);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/ui/", "/ui/issue", "/ui/release/select-projects", "/ui/release/board"})
    public void servesShellForRouterPaths(String uri) throws Exception {
        doFilter(uri);

        verify(request).getRequestDispatcher("/ui/index.html");
        verify(dispatcher).forward(request, response);
        verify(chain, never()).doFilter(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/ui/index.html",
            "/ui/assets/index-a1b2c3.js",
            "/ui/assets/index-a1b2c3.css",
            "/ui/favicon.svg"})
    public void passesThroughRealFilesUnderSpaRoot(String uri) throws Exception {
        doFilter(uri);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(dispatcher);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/session", "/api/repo/show-all", "/api/release/socket", "/job/status"})
    public void passesThroughApiAndSockets(String uri) throws Exception {
        doFilter(uri);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(dispatcher);
    }

    @Test
    public void respectsContextPath() throws Exception {
        when(request.getContextPath()).thenReturn("/release-manager");
        when(request.getRequestURI()).thenReturn("/release-manager/ui/milestone");

        filter.doFilter(request, response, chain);

        verify(dispatcher).forward(request, response);
    }
}
