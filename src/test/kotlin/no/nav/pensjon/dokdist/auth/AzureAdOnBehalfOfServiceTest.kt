package no.nav.pensjon.dokdist.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.http.*
import org.springframework.http.MediaType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import tools.jackson.databind.json.JsonMapper
import java.time.Instant

private const val ourClientId = "our-client-id"
private const val ourClientSecret = "our-client-secret"
private const val ourEndpoint = "https://loginservice.com/oauth2/token"

@RestClientTest(
    components = [AzureAdOnBehalfOfService::class],
    properties = [
        "spring.security.oauth2.client.registration.azure.clientId=$ourClientId",
        "spring.security.oauth2.client.registration.azure.client-secret=$ourClientSecret",
        "spring.security.oauth2.client.provider.azure.token-uri=$ourEndpoint",
    ]
)
class AzureAdOnBehalfOfServiceTest(
    @Autowired private val adService: AzureAdOnBehalfOfService,
    @Autowired private val mockRestServer: MockRestServiceServer,
    @Autowired private val objectMapper: JsonMapper,
) {
    private val ourToken = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "my token", Instant.now(), Instant.now().plusSeconds(100))
    private val tokenResponse = OnBehalfOfTokenResponse(
        "Bearer",
        "their-scope",
        "the-expiration",
        "the-token-we-need",
    )
    private val tokenResponseJson = objectMapper.writeValueAsString(tokenResponse)
    private val scopes = listOf("hei")

    @Test
    fun `exchanges token successfully`() {
        mockRestServer
            .expect(requestTo(ourEndpoint))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(
                content().formDataContains(
                    mapOf(
                        "client_id" to ourClientId,
                        "client_secret" to ourClientSecret,
                        "assertion" to ourToken.tokenValue,
                        "scope" to scopes.joinToString(" "),
                    )
                )
            )
            .andRespond(withSuccess(tokenResponseJson, MediaType.APPLICATION_JSON))

        assertEquals(tokenResponse, adService.exchange(ourToken, scopes))
        mockRestServer.verify()
    }

    @Test
    fun `fails for empty token response`() {
        mockRestServer.expect(requestTo(ourEndpoint))
            .andRespond(withSuccess())

        assertThrows<OnBehalfOfException> {
            adService.exchange(ourToken, scopes)
        }
        mockRestServer.verify()
    }

    @Test
    fun `handles 404 response`() {
        mockRestServer.expect(requestTo(ourEndpoint))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val exception = assertThrows<OnBehalfOfException>() {
            adService.exchange(ourToken, scopes)
        }
        assertThat(exception.message).contains("wrong URL")
        mockRestServer.verify()
    }

    @Test
    fun `handles client error responses`() {
        mockRestServer.expect(requestTo(ourEndpoint))
            .andRespond(withBadRequest())

        assertThrows<OnBehalfOfException> {
            adService.exchange(ourToken, scopes)
        }
        mockRestServer.verify()
    }

    @Test
    fun `handles server error responses`() {
        mockRestServer.expect(requestTo(ourEndpoint))
            .andRespond(withServerError())

        assertThrows<OnBehalfOfException> {
            adService.exchange(ourToken, scopes)
        }
        mockRestServer.verify()
    }
}
