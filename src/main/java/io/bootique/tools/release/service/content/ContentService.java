package io.bootique.tools.release.service.content;

import io.bootique.tools.release.util.RequestCache;

import javax.ws.rs.core.Configuration;
import java.util.Map;

public interface ContentService {

    Map<String, RequestCache<?>> getRepoCache();

    boolean haveCache(Configuration configuration);
}
