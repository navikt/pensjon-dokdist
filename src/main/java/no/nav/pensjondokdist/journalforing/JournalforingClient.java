package no.nav.pensjondokdist.journalforing;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;

import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.journalforing.dto.FerdigstillJournalpostRequest;
import no.nav.pensjondokdist.oidc.OidcProvider;

@Component
public class JournalforingClient {
    private String url;
    private static Logger logger = LoggerFactory.getLogger(JournalforingClient.class);
    private OidcProvider oidcProvider;

    public JournalforingClient(@Value("${dokarkiv.base.url}") String baseUrl, OidcProvider oidcProvider) {
        this.url = baseUrl + "/rest/journalpostapi/v1/journalpost/%s/ferdigstill";
        this.oidcProvider = oidcProvider;
    }

    Boolean execute(String journalpostId, FerdigstillJournalpostRequest request, String systemToken) {
        String requestUri =  String.format(url, journalpostId);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpResponse httpResponse = httpClient.execute(getHttpPatch(requestUri, request, systemToken));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                logger.info("Ferdigstilte journalpost: " + journalpostId);
                return true;
            } else {
                logger.error("Kunne ikke ferdigstille journalpost " + journalpostId);
                throw new PensjonDokdistException("Kunne ikke ferdigstille journalpost. StatusCode: " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Kunne ikke ferdigstille journalpost " + journalpostId);
            throw new PensjonDokdistException(e.getMessage());
        }
    }


    private HttpPatch getHttpPatch(String uri, FerdigstillJournalpostRequest request, String systemToken) {
        HttpPatch httpPatch = new HttpPatch(uri);
        httpPatch.setHeader("Accept", "application/json");
        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setHeader("Authorization", "Bearer " + systemToken);
        httpPatch.setHeader("Nav-Consumer-Token", oidcProvider.getIdToken());
        try {
            httpPatch.setEntity( new StringEntity(toJsonString(request)));
        } catch (UnsupportedEncodingException e) {
            throw new PensjonDokdistException("Serialisering av request feilet", e);
        }
        return httpPatch;
    }
}
