package no.nav.pensjon.dokdist.auth

import com.fasterxml.jackson.annotation.JsonProperty
import net.logstash.logback.marker.RawJsonAppendingMarker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.*

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Could not exchange OBO-accessToken")
class OnBehalfOfException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)
data class OnBehalfOfTokenResponse(
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("expires_in") val expiresIn: String,
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String? = null,
)

@Component
class AzureAdOnBehalfOfService(
    @Value("\${spring.security.oauth2.client.registration.azure.clientId}") private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.azure.client-secret}") private val clientSecret: String,
    @Value("\${spring.security.oauth2.client.provider.azure.token-uri}") private val endpoint: String,
    restTemplateBuilder: RestTemplateBuilder,
) {
    private val restTemplate: RestTemplate = restTemplateBuilder.build()

    val logger = LoggerFactory.getLogger(AzureAdOnBehalfOfService::class.java)

    @Throws(OnBehalfOfException::class)
    fun exchange(accessToken: OAuth2AccessToken, scope: List<String>): OnBehalfOfTokenResponse = try {
        restTemplate.exchange<OnBehalfOfTokenResponse>(
            endpoint,
            HttpMethod.POST,
            HttpEntity(
                LinkedMultiValueMap<String, String>().apply {
                    add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                    add("client_id", clientId)
                    add("client_secret", clientSecret)
                    add("assertion", accessToken.tokenValue)
                    add("scope", scope.joinToString(" "))
                    add("requested_token_use", "on_behalf_of")
                },
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                }
            )
        ).body ?: throw OnBehalfOfException("Received empty token response")

    } catch (e: HttpClientErrorException) {
        if (e.statusCode == HttpStatus.NOT_FOUND) {
            logger.error(
                RawJsonAppendingMarker("error_response", e.responseBodyAsString),
                "Got 404 when trying to exchange token using endpoint $endpoint"
            )
            throw OnBehalfOfException("Unable to exchange token, wrong URL", e)
        } else {
            logger.error(
                RawJsonAppendingMarker("error_response", e.responseBodyAsString),
                "Failed to exchange token for scope=${scope.joinToString(" ")}, got status=${e.statusText}, message=${e.message}"
            )
            throw OnBehalfOfException("Unable to exchange token", e)
        }
    } catch (e: HttpServerErrorException) {
        logger.error(
            RawJsonAppendingMarker("error_response", e.responseBodyAsString),
            "Failed to exchange token, got status=${e.statusText}, message=${e.message}"
        )
        throw OnBehalfOfException("Unable to exchange token", e)
    }
}
