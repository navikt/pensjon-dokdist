package no.nav.pensjondokdist.oidc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.util.JsonUtil;
import no.nav.pensjondokdist.util.JwtUtil;
import no.nav.pensjondokdist.util.SecretUtil;

@Component
public class OidcProvider {
    private String uri;
    private OAuth2AuthorizedClientService authorizedClientService;
    private String refreshToken;
    private String navCallid;

    public OidcProvider(@Value("${spring.security.oauth2.client.provider.openam.isso-host}") String uri,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.uri = uri;
        this.authorizedClientService = authorizedClientService;
    }

    public String getIdToken() {
        String idToken = getToken();
        if (Instant.now().getEpochSecond() - JwtUtil.getExpirationTime(idToken).getEpochSecond() > 0) {
            return refreshToken();
        }
        return idToken;
    }

    public HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + getIdToken());
        httpHeaders.add("Nav-Consumer-Id", "pensjon-dokdist");
        httpHeaders.add("Nav-Callid", getNavCallid());
        return httpHeaders;
    }

    private String getNavCallid() {
        if (navCallid == null) {
            navCallid = UUID.randomUUID().toString();
        }
        return navCallid;
    }
    private HttpRequestBase createRefreshTokenRequest() {
        String oidcClientName = JwtUtil.getClientName(getToken());
        String realm = "/";

        HttpPost request = new HttpPost(uri + "/access_token");
        try {
            request.setHeader("Authorization", basicCredentials(oidcClientName, SecretUtil.readSecret("oidc/client_secret")));

            request.setHeader("Cache-Control", "no-cache");
            request.setHeader("Content-type", "application/x-www-form-urlencoded");
            String data = "grant_type=refresh_token"
                    + "&scope=openid"
                    + "&realm=" + realm
                    + "&refresh_token=" + getRefreshToken();
            request.setEntity(new StringEntity(data, StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            throw new PensjonDokdistException(e.getMessage(), e);
        }
        return request;
    }

    private String refreshToken() {
        if (refreshToken!= null && JwtUtil.getExpirationTime(refreshToken).getEpochSecond() - Instant.now().getEpochSecond() > 0) {
            return refreshToken;
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpRequestBase request = createRefreshTokenRequest();
            try (CloseableHttpResponse response = client.execute(request)) {
                String responseString = responseText(response);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.OK.value()) {
                    OidcTokenHolder oidcTokenHolder = JsonUtil.toObjectFromJsonString(responseString, new TypeReference<OidcTokenHolder>() {
                    });
                    refreshToken = oidcTokenHolder.getId_token();
                    return refreshToken;
                }
                throw new PensjonDokdistException("token ikke funnet.");
            } finally {
                request.reset();
            }
        } catch (IOException e) {
            throw new PensjonDokdistException(e.getMessage(), e);
        }
    }

    private String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof OidcUser) {
            return ((OidcUser) authentication.getPrincipal()).getIdToken().getTokenValue();
        } else {
            throw new PensjonDokdistException("Token ikke funnet.");
        }
    }

    private String getRefreshToken() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        return Objects.requireNonNull(client.getRefreshToken()).getTokenValue();
    }

    private String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));
    }

    private String responseText(CloseableHttpResponse response) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
            try (BufferedReader br = new BufferedReader(isr)) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
