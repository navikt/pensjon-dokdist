package no.nav.pensjondokdist.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Test;

public class JwtUtilTest {

    /* JWT-body
    {
         "at_hash": "8w_KiL4KeIzEEjNFvWOBZg",
         "sub": "Z992960",
         "auditTrackingId": "b9b83b77-a1f3-4130-816b-0199e8db7024-26057236",
         "iss": "https://isso-q.adeo.no:443/isso/oauth2",
         "tokenName": "id_token",
         "nonce": "fimbSJJTS4HJDspFfdF4TCXveRKBniF60pybHD7Hl_A",
         "aud": "pensjon-dokdist-localhost",
         "c_hash": "p1z80r2R6nUZMp2WHQghqA",
         "org.forgerock.openidconnect.ops": "d4ce8cb7-bde6-4a1b-b249-5b60a336e9ab",
         "azp": "pensjon-dokdist-localhost",
         "auth_time": 1590565340,
         "realm": "/",
         "exp": 1590568940,
         "tokenType": "JWTToken",
         "iat": 1590565340
     }
     */

    @Test
    public void shouldGetClientNameFromAuthorizedPartyClaim() {
        String jwt =
                "eyAidHlwIjogIkpXVCIsICJraWQiOiAiMWwySmtDb1RMMTBibWVBeHlsZzR4Umk4ajJZPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICI4d19LaUw0S2VJekVFak5GdldPQlpnIiwgInN1YiI6ICJaOTkyOTYwIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJiOWI4M2I3Ny1hMWYzLTQxMzAtODE2Yi0wMTk5ZThkYjcwMjQtMjYwNTcyMzYiLCAiaXNzIjogImh0dHBzOi8vaXNzby1xLmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJub25jZSI6ICJmaW1iU0pKVFM0SEpEc3BGZmRGNFRDWHZlUktCbmlGNjBweWJIRDdIbF9BIiwgImF1ZCI6ICJwZW5zam9uLWRva2Rpc3QtbG9jYWxob3N0IiwgImNfaGFzaCI6ICJwMXo4MHIyUjZuVVpNcDJXSFFnaHFBIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiZDRjZThjYjctYmRlNi00YTFiLWIyNDktNWI2MGEzMzZlOWFiIiwgImF6cCI6ICJwZW5zam9uLWRva2Rpc3QtbG9jYWxob3N0IiwgImF1dGhfdGltZSI6IDE1OTA1NjUzNDAsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE1OTA1Njg5NDAsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTU5MDU2NTM0MCB9.a65n_7fgTY-qiG0oR79xA1N7Plq6ttEAd9XejmWRII3SM3gEWJSqD_PDwqEhkAJ5KacnZ2yqBznKU22ivGY0hO38AzCna6Hwsjff_zWlitJMEasVgCiOOqbLxsDlEDnukFW5CPjrLi9x7mOamjIJTKRzSCKNtUEEjBh6DZ0A-RH8Y_i8kDyMS-5rLPbVeB1CvusxW5frRqgzq7-ZiPF7wb6iG_KOb_EY9elCqOq2WAPpJN6mjD9eeBpKcRk4Ra-XBrzhABWekiEgv9lyA-Q1yKU2Vll3hQH1tscVmXXn6PTNAI_vtjcrcbhtyB7NL4TAN3QPYSEI34NIqdbb6Ziv_w";

        String clientName = JwtUtil.getClientName(jwt);

        assertThat(clientName).isEqualTo("pensjon-dokdist-localhost");
    }

    @Test
    public void shouldGetExpirationtimeFromExpirationTimeClaim() {
        String jwt =
                "eyAidHlwIjogIkpXVCIsICJraWQiOiAiMWwySmtDb1RMMTBibWVBeHlsZzR4Umk4ajJZPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICI4d19LaUw0S2VJekVFak5GdldPQlpnIiwgInN1YiI6ICJaOTkyOTYwIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJiOWI4M2I3Ny1hMWYzLTQxMzAtODE2Yi0wMTk5ZThkYjcwMjQtMjYwNTcyMzYiLCAiaXNzIjogImh0dHBzOi8vaXNzby1xLmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJub25jZSI6ICJmaW1iU0pKVFM0SEpEc3BGZmRGNFRDWHZlUktCbmlGNjBweWJIRDdIbF9BIiwgImF1ZCI6ICJwZW5zam9uLWRva2Rpc3QtbG9jYWxob3N0IiwgImNfaGFzaCI6ICJwMXo4MHIyUjZuVVpNcDJXSFFnaHFBIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiZDRjZThjYjctYmRlNi00YTFiLWIyNDktNWI2MGEzMzZlOWFiIiwgImF6cCI6ICJwZW5zam9uLWRva2Rpc3QtbG9jYWxob3N0IiwgImF1dGhfdGltZSI6IDE1OTA1NjUzNDAsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE1OTA1Njg5NDAsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTU5MDU2NTM0MCB9.a65n_7fgTY-qiG0oR79xA1N7Plq6ttEAd9XejmWRII3SM3gEWJSqD_PDwqEhkAJ5KacnZ2yqBznKU22ivGY0hO38AzCna6Hwsjff_zWlitJMEasVgCiOOqbLxsDlEDnukFW5CPjrLi9x7mOamjIJTKRzSCKNtUEEjBh6DZ0A-RH8Y_i8kDyMS-5rLPbVeB1CvusxW5frRqgzq7-ZiPF7wb6iG_KOb_EY9elCqOq2WAPpJN6mjD9eeBpKcRk4Ra-XBrzhABWekiEgv9lyA-Q1yKU2Vll3hQH1tscVmXXn6PTNAI_vtjcrcbhtyB7NL4TAN3QPYSEI34NIqdbb6Ziv_w";

        Instant expirationTime = JwtUtil.getExpirationTime(jwt);
        assertThat(expirationTime.getEpochSecond()).isEqualTo(1590568940);
    }
}
