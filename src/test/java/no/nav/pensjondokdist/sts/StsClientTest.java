package no.nav.pensjondokdist.sts;

import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import no.nav.pensjondokdist.PensjonDokdistException;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class StsClientTest {
    private static final String URL = "http://StsUrl.com";
    private StsClient client;
    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void init() {
        client = new StsClient(URL, restTemplate);
    }

    @Ignore
    @Test
    public void testGetWithSuccessResponse() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withSuccess(jsonString(), MediaType.APPLICATION_JSON));
        client.get();
        server.verify();
    }

    @Test(expected = PensjonDokdistException.class)
    public void testGetWithNullBodyShouldThrowException() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        client.get();
        server.verify();
    }

    @Test(expected = PensjonDokdistException.class)
    public void testGetWithBadRequestResponseThrowException() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withBadRequest());
        client.get();
        server.verify();
    }

    private String jsonString() {
        return "{\n"
                + "  \"access_token\" : \"token\",\n"
                + "  \"token_type\" : \"Bearer\",\n"
                + "  \"expires_in\" : 3600\n"
                + "}";
    }
}
