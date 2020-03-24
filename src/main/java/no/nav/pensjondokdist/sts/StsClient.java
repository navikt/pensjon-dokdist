package no.nav.pensjondokdist.sts;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.util.SecretUtil;

@Component
public class StsClient {
    private String url;
    private RestTemplate restTemplate;

    public StsClient(@Value("${sts.url}") String url, RestTemplate restTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
    }

    String get() {
        try {
            ResponseEntity<ServiceBrukerToken> entity =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, getHeaders()), ServiceBrukerToken.class);
            if (entity.getStatusCode() == HttpStatus.OK && entity.getBody() != null) {
                return entity.getBody().getAccess_token();
            } else {
                throw new PensjonDokdistException("Hent servicebruker token feilet. StatusCode: " + entity.getStatusCode().value());
            }
        } catch (Exception e) {
            throw new PensjonDokdistException("Hent servicebruker token feilet. " + e.getMessage());
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Basic " + getBasicAuth());
        return httpHeaders;
    }

    private String getBasicAuth() {
        try {
            String input = SecretUtil.readSecret("serviceuser/username") + ":" + SecretUtil.readSecret("serviceuser/password");
            return Base64.getEncoder().encodeToString(input.getBytes());
        } catch (IOException e) {
            throw new PensjonDokdistException(e.getMessage());
        }
    }
}
