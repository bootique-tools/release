package io.bootique.tools.release.service.graphql;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Map;

public interface GraphQLService {

    <T> T query(Class<T> resultType, URI endpoint, String authToken, String query, Map<String, Object> variables);

    JsonNode queryJson(URI endpoint, String authToken, String query, Map<String, Object> variables) throws Exception;

    void mutation(URI endpoint, String authToken, String mutation);

    void mutation(URI endpoint, String authToken, String mutation, Map<String, String> variables);

}
