package io.bootique.tools.release.service.validation;

import java.util.List;
import java.util.Map;

public interface ValidatePomService {

    Map<String, List<String>> validatePom(String repoName);

}
