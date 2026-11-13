package no.nav.pensjon.dokdist.auth

import no.nav.pensjon.dokdist.filter.CALL_ID_HEADER
import no.nav.pensjon.dokdist.filter.CALL_ID_MDC_KEY
import org.slf4j.MDC
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.http.client.*
import org.springframework.web.client.RestTemplate

/**
 * ApiBinding for endpoints that accept AzureAD On-Behalf-Of tokens.
 */
abstract class AzureAdOBOApiBinding(accessToken: OnBehalfOfTokenResponse, restTemplateBuilder: RestTemplateBuilder) {
    protected val restTemplate: RestTemplate = restTemplateBuilder.interceptors(bearerTokenInterceptor(accessToken.accessToken), callIdInterceptor()).build()

    private fun bearerTokenInterceptor(accessToken: String) =
        ClientHttpRequestInterceptor { request, body, execution ->
            request.headers.add(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            return@ClientHttpRequestInterceptor execution.execute(request, body)
        }

    private fun callIdInterceptor() =
        ClientHttpRequestInterceptor { request, body, execution ->
            MDC.get(CALL_ID_MDC_KEY)?.let { request.headers.set(CALL_ID_HEADER, it) }
            execution.execute(request, body)
        }
}
