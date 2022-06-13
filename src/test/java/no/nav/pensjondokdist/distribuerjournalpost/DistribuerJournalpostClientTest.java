package no.nav.pensjondokdist.distribuerjournalpost;

import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostRequest;
import no.nav.pensjondokdist.oidc.OidcProvider;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class DistribuerJournalpostClientTest {
    private static final String URL = "http://SafUrl.com";
    private DistribuerJournalpostClient client;
    @Mock
    private OidcProvider provider;
    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void init() {
        client = new DistribuerJournalpostClient(URL, restTemplate, provider);
    }

    @Test
    public void testGetWithSuccessResponse() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        client.post(new DistribuerJournalpostRequest("12345", null, null, null, null, null, null));
        server.verify();
    }

    @Test(expected = PensjonDokdistException.class)
    public void testGetWithBadRequestResponseThrowException() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(manyTimes(), requestTo(URL))
                .andRespond(withBadRequest());
        client.post(new DistribuerJournalpostRequest("12345", null, null, null, null, null, null));
        server.verify();
    }
}
