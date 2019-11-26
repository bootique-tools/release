package io.bootique.tools.release.service.validation;

import java.util.List;

public interface ValidatePomService {

    List<String> validatePom(String repoName);

}
