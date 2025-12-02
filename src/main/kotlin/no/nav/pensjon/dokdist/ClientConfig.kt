package no.nav.pensjon.dokdist

import no.nav.pensjon.dokdist.auth.*
import no.nav.pensjon.dokdist.dokarkiv.*
import no.nav.pensjon.dokdist.dokdistfordeling.*
import no.nav.pensjon.dokdist.saf.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.context.annotation.RequestScope

class AuthorizationError(message: String) : RuntimeException(message)

@Configuration
class ClientConfig {

    @Bean
    @RequestScope
    fun safClient(
        @Value("\${saf.url}") url: String,
        @Value("\${saf.scope}") scope: String,
        clientService: OAuth2AuthorizedClientService,
        tokenExchangeService: AzureAdOnBehalfOfService,
    ): SafService =
        SafClient(url, exchangeOnBehalfOfToken(scope, clientService, tokenExchangeService))

    @Bean
    @RequestScope
    fun dokarkivClient(
        @Value("\${dokarkiv.url}") url: String,
        @Value("\${dokarkiv.scope}") scope: String,
        clientService: OAuth2AuthorizedClientService,
        tokenExchangeService: AzureAdOnBehalfOfService,
    ): DokarkivService =
        DokarkivClient(url, exchangeOnBehalfOfToken(scope, clientService, tokenExchangeService))

    @Bean
    @RequestScope
    fun distribuerJournalpostClient(
        @Value("\${distribuer-journalpost.url}") url: String,
        @Value("\${distribuer-journalpost.scope}") scope: String,
        clientService: OAuth2AuthorizedClientService,
        tokenExchangeService: AzureAdOnBehalfOfService,
    ): DistribuerJournalpostService =
        DistribuerJournalpostClient(url, exchangeOnBehalfOfToken(scope, clientService, tokenExchangeService))

    private fun exchangeOnBehalfOfToken(scope: String, clientService: OAuth2AuthorizedClientService, tokenExchangeService: AzureAdOnBehalfOfService): OnBehalfOfTokenResponse {
        val auth = SecurityContextHolder.getContext().authentication

        return if (auth is OAuth2AuthenticationToken) {
            clientService.loadAuthorizedClient<OAuth2AuthorizedClient>(auth.authorizedClientRegistrationId, auth.name).let {
                tokenExchangeService.exchange(it.accessToken, listOf(scope))
            }
        } else {
            throw AuthorizationError("Expected OAuth2AuthenticationToken but was: ${auth::class.qualifiedName}")
        }
    }
}
