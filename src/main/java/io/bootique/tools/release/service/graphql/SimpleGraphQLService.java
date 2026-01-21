package io.bootique.tools.release.service.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple implementation of GraphQL client compatible with GitHub API.
 */
public class SimpleGraphQLService implements GraphQLService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLService.class);

    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public SimpleGraphQLService() throws Exception {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());

        HttpClientTransportDynamic transport = new HttpClientTransportDynamic();
        httpClient = new HttpClient(transport);
        httpClient.start();
    }

    @Override
    public <T> T query(Class<T> resultType, URI endpoint, String authToken, String query, Map<String, Object> variables) {
        try {
            JsonNode root = queryJson(endpoint, authToken, query, variables);
            return mapper.treeToValue(root.get("data"), resultType);
        } catch (Exception e) {
            LOGGER.error("Unable to deserialize result of query", e);
            return null;
        }
    }

    @Override
    public JsonNode queryJson(URI endpoint, String authToken, String query, Map<String, Object> variables) throws Exception {
        byte[] response = sendRequest(endpoint, authToken, query, variables);
        return mapper.readTree(response);
    }

    private byte[] sendRequest(URI endpoint, String authToken, String query, Map<String, Object> variables) throws Exception {
        Request request = httpClient.POST(Objects.requireNonNull(endpoint));
        request.headers(fields -> fields.add(HttpHeader.AUTHORIZATION, "Bearer " + Objects.requireNonNull(authToken)));
        request.body(new StringRequestContent(queryToJsonString(query, variables)));
        ContentResponse response = request.send();
        if(response.getStatus() != 200) {
            throw new Exception(response.getContentAsString());
        }
        return response.getContent();
    }

    private String queryToJsonString(String query, Map<String, Object> variables) throws JsonProcessingException {
        Map<String, Object> fullQuery = new HashMap<>();
        fullQuery.put("query", Objects.requireNonNull(query));
        fullQuery.put("variables", Objects.requireNonNull(variables));
        return mapper.writeValueAsString(fullQuery);
    }

    @Override
    public void mutation(URI endpoint, String authToken, String mutation) {
        mutation(endpoint, authToken, mutation, Collections.emptyMap());
    }

    @Override
    public void mutation(URI endpoint, String authToken, String mutation, Map<String, String> variables) {
        throw new UnsupportedOperationException("Mutations are not supported by this implementation.");
    }
}
