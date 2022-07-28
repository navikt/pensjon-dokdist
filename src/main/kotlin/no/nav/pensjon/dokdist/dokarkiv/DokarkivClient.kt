package no.nav.pensjon.dokdist.dokarkiv

import no.nav.pensjon.dokdist.auth.*
import org.slf4j.*
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.*
import org.springframework.web.client.HttpClientErrorException.*
import org.springframework.web.server.ResponseStatusException

interface DokarkivService {
    fun ferdigstillJournalpost(journalpostId: String, journalfoerendeEnhet: String)
}

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Klarte ikke å ferdigstille journalpost")
class DokarkivException(message: String?, cause: Throwable? = null) : Exception(message, cause)

private data class FerdigstillJournalpostRequest(val journalfoerendeEnhet: String)

class DokarkivClient(
    private val url: String,
    accessToken: OnBehalfOfTokenResponse,
    restTemplateBuilder: RestTemplateBuilder = RestTemplateBuilder(),
) : AzureAdOBOApiBinding(accessToken, restTemplateBuilder), DokarkivService {

    override fun ferdigstillJournalpost(journalpostId: String, journalfoerendeEnhet: String) {
        try {
            restTemplate.exchange<String>(
                "$url/rest/journalpostapi/v1/journalpost/$journalpostId/ferdigstill",
                HttpMethod.PATCH,
                HttpEntity(FerdigstillJournalpostRequest(journalfoerendeEnhet)),
            )
        } catch (e: BadRequest) {
            val msg = "Kunne ikke ferdigstille journalpost $journalpostId: ${e.responseBodyAsString}"
            logger.warn(msg, e)
            throw DokarkivException(msg, e)
        } catch (e: Forbidden) {
            val msg = "Ikke tilgang til å ferdigstille journalpost: $journalpostId"
            logger.warn(msg, e)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, msg, e)
        } catch (e: RestClientException) {
            val msg = "Ukjent feil ved ferdigstilling av journalpost: $journalpostId"
            logger.error(msg, e)
            throw DokarkivException(msg, e)
        }

        logger.info("Ferdigstilte journalpost: $journalpostId")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DokarkivClient::class.java)
    }
}
