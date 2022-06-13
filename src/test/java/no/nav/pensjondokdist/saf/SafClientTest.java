package no.nav.pensjondokdist.saf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.oidc.OidcProvider;
import no.nav.pensjondokdist.saf.graphql.GraphQlError;
import no.nav.pensjondokdist.saf.graphql.GraphQlResponse;
import no.nav.pensjondokdist.util.JsonUtil;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class SafClientTest {
    private static final String URL = "http://SafUrl.com";
    private SafClient client;
    @Mock
    private OidcProvider provider;
    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void init() throws IOException {
        client = new SafClient(URL, restTemplate, provider);
   }

    @Test
    public void testGetWithSuccessResponse() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withSuccess(getJsonString(), MediaType.APPLICATION_JSON));
        client.fetchJournal("12345");
        server.verify();
    }

    @Test(expected = PensjonDokdistException.class)
    public void testGetWithNullBodyShouldThrowException() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        client.fetchJournal("12345");
        server.verify();
    }

    @Test(expected = PensjonDokdistException.class)
    public void testGetWithBadRequestResponseThrowException() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withBadRequest());
        client.fetchJournal("12345");
        server.verify();
    }

    @Test
    public void testErrorResponse() {
        GraphQlResponse graphQlResponse = JsonUtil.toObjectFromJsonString(getErrorString(),  new TypeReference<GraphQlResponse>(){});
        assertThat(graphQlResponse).isNotNull();
        assertThat(graphQlResponse.getErrors()).hasSize(1);
        GraphQlError error = graphQlResponse.getErrors().get(0);
        assertThat(error).isNotNull();
        assertThat(error.getMessage()).isNotNull();
        assertThat(error.getExceptionType()).isEqualTo("safexception");
        assertThat(error.getException()).isEqualTo(NullPointerException.class.getSimpleName());
    }

    private String getErrorString() {
        return "{\n"
                + "  \"errors\": [\n"
                + "    {\n"
                + "      \"message\": \"Feilet ved henting av data (/journalpost) : null\",\n"
                + "      \"locations\": [\n"
                + "        {\n"
                + "          \"line\": 3,\n"
                + "          \"column\": 3\n"
                + "        }\n"
                + "      ],\n"
                + "      \"path\": [\n"
                + "        \"journalpost\"\n"
                + "      ],\n"
                + "      \"exceptionType\": \"safexception\",\n"
                + "      \"exception\": \"NullPointerException\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"data\": {\n"
                + "    \"journalpost\": null\n"
                + "  }\n"
                + "}";
    }
    private String getJsonString() {
        return "{\"data\":{\"journalpost\":{\"journalpostId\":\"453501014\",\"journalforendeEnhet\":\"4803\"}}}";
    }
}
