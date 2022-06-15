package no.nav.pensjondokdist.distribuerjournalpost;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostRequest;
import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostResponse;
import no.nav.pensjondokdist.oidc.OidcProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DistribuerJournalpostClient {
    private String url;
    private RestTemplate restTemplate;
    private OidcProvider oidcProvider;

    private static Logger logger = LoggerFactory.getLogger(DistribuerJournalpostClient.class);

    public DistribuerJournalpostClient(@Value("${distribuer.journalpost.v1.url}") String url, RestTemplate restTemplate, OidcProvider oidcProvider) {
        this.url = url;
        this.restTemplate = restTemplate;
        this.oidcProvider = oidcProvider;
    }

    DistribuerJournalpostResponse post(DistribuerJournalpostRequest request) {
        try {
            logger.info("url: " + url + "request: " + new ObjectMapper().writeValueAsString(request)); //TODO remove.
            return restTemplate.postForObject(url, new HttpEntity<>(request, oidcProvider.getHeaders()), DistribuerJournalpostResponse.class);
        } catch (Exception e) {
            logger.error("Kunne ikke distribuere journalpost for journalpostId: " + request.getJournalpostId());
            throw new PensjonDokdistException("Kunne ikke distribuere jounalpost.  for journalpostId: " +
                    request.getJournalpostId() + " " + e.getMessage());
        }
    }
}
