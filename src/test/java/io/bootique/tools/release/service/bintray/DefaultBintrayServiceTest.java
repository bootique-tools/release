package io.bootique.tools.release.service.bintray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.bootique.tools.release.model.persistent.Organization;
import io.bootique.tools.release.model.persistent.Repository;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultBintrayServiceTest {

    private DefaultBintrayApi defaultBintrayApi;
    private MockPreferenceService mockPreferenceService = new MockPreferenceService();

    @BeforeEach
    void createService() {
        defaultBintrayApi = new DefaultBintrayApi();
        mockPreferenceService.set(BintrayApi.BINTRAY_ORG_NAME, "stariy95");
        defaultBintrayApi.preferenceService = mockPreferenceService;
        defaultBintrayApi.targets = targets -> ClientBuilder.newClient().target("https://api.bintray.com");
    }

    @Test
    @DisplayName("Bintray request test.")
    void testBintrayRequest() {
        Repository repository = new Repository();
        repository.setName("dummy-api");
        boolean response = defaultBintrayApi.getRepository(repository);
        assertTrue(response);
    }

    @Test
    @DisplayName("Json parsing test")
    void testJsonParsing() throws IOException {
        String json = "{\n" +
                "  \"data\": {\n" +
                "    \"organization\": {\n" +
                "      \"id\": \"MDEyOk9yZ2FuaXphdGlvbjQwOTE2MTg3\",\n" +
                "      \"url\": \"https://github.com/dummy-org-00\",\n" +
                "      \"login\": \"dummy-org-00\",\n" +
                "      \"name\": \"dummy-org-00\",\n" +
                "      \"repositories\": {\n" +
                "        \"totalCount\": 7,\n" +
                "        \"nodes\": [\n" +
                "          {\n" +
                "            \"name\": \"dummy-api\",\n" +
                "            \"description\": \"Test description\",\n" +
                "            \"id\": \"MDEwOlJlcG9zaXRvcnkxNDAwODY4MDc=\",\n" +
                "            \"url\": \"https://github.com/dummy-org-00/dummy-api\",\n" +
                "            \"sshUrl\": \"git@github.com:dummy-org-00/dummy-api.git\",\n" +
                "            \"updatedAt\": \"2018-08-07T08:54:22Z\",\n" +
                "            \"pushedAt\": \"2018-08-07T08:54:21Z\",\n" +
                "            \"parent\": null,\n" +
                "            \"pullRequests\": {\n" +
                "              \"totalCount\": 0,\n" +
                "              \"nodes\": []\n" +
                "            },\n" +
                "            \"issues\": {\n" +
                "              \"totalCount\": 2,\n" +
                "              \"nodes\": [\n" +
                "                {\n" +
                "                  \"id\": \"MDU6SXNzdWUzMzk3NDI5MjI=\",\n" +
                "                  \"url\": \"https://github.com/dummy-org-00/dummy-api/issues/1\",\n" +
                "                  \"number\": 1,\n" +
                "                  \"title\": \"Dummy issue 4\",\n" +
                "                  \"createdAt\": \"2018-07-10T08:32:22Z\",\n" +
                "                  \"comments\": {\n" +
                "                    \"totalCount\": 0\n" +
                "                  },\n" +
                "                  \"author\": {\n" +
                "                    \"__typename\": \"User\",\n" +
                "                    \"login\": \"stariy95\",\n" +
                "                    \"url\": \"https://github.com/stariy95\",\n" +
                "                    \"id\": \"MDQ6VXNlcjUxODgyMTA=\",\n" +
                "                    \"name\": \"Nikita Timofeev\"\n" +
                "                  },\n" +
                "                  \"milestone\": {\n" +
                "                    \"id\": \"MDk6TWlsZXN0b25lMzQ4NTAyNw==\",\n" +
                "                    \"url\": \"https://github.com/dummy-org-00/dummy-api/milestone/1\",\n" +
                "                    \"title\": \"1.1\",\n" +
                "                    \"number\": 1\n" +
                "                  },\n" +
                "                  \"labels\": {\n" +
                "                    \"totalCount\": 1,\n" +
                "                    \"nodes\": [\n" +
                "                      {\n" +
                "                        \"id\": \"MDU6TGFiZWw5ODY3Mjk4MTA=\",\n" +
                "                        \"url\": \"https://github.com/dummy-org-00/dummy-api/labels/bug\",\n" +
                "                        \"name\": \"bug\",\n" +
                "                        \"color\": \"d73a4a\"\n" +
                "                      }\n" +
                "                    ]\n" +
                "                  }\n" +
                "                },\n" +
                "                {\n" +
                "                  \"id\": \"MDU6SXNzdWUzMzk3NDMwMTc=\",\n" +
                "                  \"url\": \"https://github.com/dummy-org-00/dummy-api/issues/2\",\n" +
                "                  \"number\": 2,\n" +
                "                  \"title\": \"Dummy issue 5\",\n" +
                "                  \"createdAt\": \"2018-07-10T08:32:39Z\",\n" +
                "                  \"comments\": {\n" +
                "                    \"totalCount\": 0\n" +
                "                  },\n" +
                "                  \"author\": {\n" +
                "                    \"__typename\": \"User\",\n" +
                "                    \"login\": \"stariy95\",\n" +
                "                    \"url\": \"https://github.com/stariy95\",\n" +
                "                    \"id\": \"MDQ6VXNlcjUxODgyMTA=\",\n" +
                "                    \"name\": \"Nikita Timofeev\"\n" +
                "                  },\n" +
                "                  \"milestone\": {\n" +
                "                    \"id\": \"MDk6TWlsZXN0b25lMzQ4NTAyNw==\",\n" +
                "                    \"url\": \"https://github.com/dummy-org-00/dummy-api/milestone/1\",\n" +
                "                    \"title\": \"1.1\",\n" +
                "                    \"number\": 1\n" +
                "                  },\n" +
                "                  \"labels\": {\n" +
                "                    \"totalCount\": 1,\n" +
                "                    \"nodes\": [\n" +
                "                      {\n" +
                "                        \"id\": \"MDU6TGFiZWw5ODY3Mjk4MTY=\",\n" +
                "                        \"url\": \"https://github.com/dummy-org-00/dummy-api/labels/question\",\n" +
                "                        \"name\": \"question\",\n" +
                "                        \"color\": \"d876e3\"\n" +
                "                      }\n" +
                "                    ]\n" +
                "                  }\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"milestones\": {\n" +
                "              \"totalCount\": 4,\n" +
                "              \"nodes\": [\n" +
                "                {\n" +
                "                  \"id\": \"MDk6TWlsZXN0b25lMzU1ODEyMQ==\",\n" +
                "                  \"url\": \"https://github.com/dummy-org-00/dummy-api/milestone/9\",\n" +
                "                  \"title\": \"2.0\",\n" +
                "                  \"number\": 9\n" +
                "                },\n" +
                "                {\n" +
                "                  \"id\": \"MDk6TWlsZXN0b25lMzU1ODEyOQ==\",\n" +
                "                  \"url\": \"https://github.com/dummy-org-00/dummy-api/milestone/10\",\n" +
                "                  \"title\": \"2.1\",\n" +
                "                  \"number\": 10\n" +
                "                },\n" +
                "                {\n" +
                "                  \"id\": \"MDk6TWlsZXN0b25lMzU1ODEzNA==\",\n" +
                "                  \"url\": \"https://github.com/dummy-org-00/dummy-api/milestone/11\",\n" +
                "                  \"title\": \"2.2\",\n" +
                "                  \"number\": 11\n" +
                "                },\n" +
                "                {\n" +
                "                  \"id\": \"MDk6TWlsZXN0b25lMzU2NTA3Nw==\",\n" +
                "                  \"url\": \"https://github.com/dummy-org-00/dummy-api/milestone/16\",\n" +
                "                  \"title\": \"2.7\",\n" +
                "                  \"number\": 16\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());
        JsonNode jsonNode = objectMapper.readTree(json);

        Organization organization = objectMapper.treeToValue(jsonNode.get("data").get("organization"), Organization.class);

        assertEquals(organization.getName(), "dummy-org-00");
        assertEquals(organization.getRepositoryCollection().getRepositories().size(), 1);
        assertEquals(organization.getRepositoryCollection().getRepositories().get(0).getName(), "dummy-api");
        assertEquals(organization.getRepositoryCollection().getRepositories().get(0).getMilestoneCollection().getTotalCount(), 4);
        assertEquals(organization.getRepositoryCollection().getRepositories().get(0).getIssueCollection().getTotalCount(), 2);
    }
}
