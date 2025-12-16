package no.nav.pensjon.dokdist.auth

import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.http.client.*
import org.springframework.web.client.RestTemplate

/**
 * ApiBinding for endpoints that accept AzureAD On-Behalf-Of tokens.
 */
abstract class AzureAdOBOApiBinding(accessToken: OnBehalfOfTokenResponse, restTemplateBuilder: RestTemplateBuilder) {
    protected val restTemplate: RestTemplate = restTemplateBuilder.interceptors(bearerTokenInterceptor(accessToken.accessToken)).build()

    private fun bearerTokenInterceptor(accessToken: String) =
        ClientHttpRequestInterceptor { request, body, execution ->
            request.headers.add(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            return@ClientHttpRequestInterceptor execution.execute(request, body)
        }
}
