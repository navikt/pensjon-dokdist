package no.nav.pensjondokdist.saf;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;
import static no.nav.pensjondokdist.util.JsonUtil.toObjectFromJsonString;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import no.nav.pensjondokdist.saf.model.Journalpost;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.oidc.OidcProvider;
import no.nav.pensjondokdist.saf.graphql.GraphQlError;
import no.nav.pensjondokdist.saf.graphql.GraphQlRequest;
import no.nav.pensjondokdist.saf.graphql.GraphQlResponse;
import no.nav.pensjondokdist.saf.graphql.Variables;

@Component
public class SafClient {
    private String url;
    private String query;
    private RestTemplate restTemplate;
    private OidcProvider oidcProvider;
    private static Logger logger = LoggerFactory.getLogger(SafClient.class);

    public SafClient(@Value("${saf.graphql}") String url, RestTemplate restTemplate, OidcProvider oidcProvider) throws IOException {
        this.url = url;
        this.restTemplate = restTemplate;
        this.oidcProvider = oidcProvider;
        query = StreamUtils.copyToString(new ClassPathResource("saf/journalpostQuery.graphql").getInputStream(), Charsets.UTF_8);
    }

    Journalpost fetchJournal(String journalpostId) {
        GraphQlRequest graphQlRequest = new GraphQlRequest(query, new Variables(journalpostId));
        try {
            ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(toJsonString(graphQlRequest), oidcProvider.getHeaders()),
                    String.class);
            return handleResponse(entity, journalpostId);
        } catch (Exception e) {
            logger.error("Kunne ikke hente journalpost for journalpostId " + journalpostId);
            throw new PensjonDokdistException("Kunne ikke hente journalpost for journalpostId " + journalpostId, e);
        }
    }

    private Journalpost handleResponse(ResponseEntity<String> entity, String journalpostId) {
        if (entity.getStatusCode() == HttpStatus.OK && entity.getBody() != null) {
            GraphQlResponse graphQlResponse = toObjectFromJsonString(entity.getBody(), new TypeReference<GraphQlResponse>() {
            });
            if (graphQlResponse.getData() == null || graphQlResponse.getData().getJournalpost() == null) {
                List<String> errorMessageList = graphQlResponse.getErrors()
                        .stream()
                        .map(GraphQlError::getMessage)
                        .collect(Collectors.toList());
                StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
                stringJoiner.add("Journalpost for journalpostId " + journalpostId + " er null.");
                errorMessageList.forEach(stringJoiner::add);
                String errors = errorMessageList.toString();
                logger.error(errors);
                throw new PensjonDokdistException(errors);
            }
            return graphQlResponse.getData().getJournalpost();
        } else {
            logger.error("Fant ikke journalpost: " + journalpostId);
            throw new PensjonDokdistException("Fant ikke journalpost. StatusCode: " + entity.getStatusCode().value());
        }
    }
}
