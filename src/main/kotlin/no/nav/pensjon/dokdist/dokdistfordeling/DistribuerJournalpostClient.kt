package no.nav.pensjon.dokdist.dokdistfordeling

import no.nav.pensjon.dokdist.*
import no.nav.pensjon.dokdist.auth.*
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostRequest.*
import org.slf4j.*
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.exchange
import java.time.Duration

const val BESTILLENDE_FAGSYSTEM = "AT05"

val DEFAULT_DISTRIBUER_BACKOFF: List<Duration> = listOf(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(2))

data class DistribueringInternalResponse(
    val originalResponse: DistribuerJournalpostResponse?,
    val status: Status
)

enum class Status { CONFLICT, MOTTAKER_DOED, OK }

interface DistribuerJournalpostService {
    fun distribuer(journalpostId: String, distribusjonstype: Distribusjonstype): DistribueringInternalResponse
}

class DistribuerJournalpostClient(
    private val url: String,
    accessToken: OnBehalfOfTokenResponse,
    restTemplateBuilder: RestTemplateBuilder = RestTemplateBuilder(),
    private val backoff: List<Duration> = DEFAULT_DISTRIBUER_BACKOFF,
) : AzureAdOBOApiBinding(accessToken, restTemplateBuilder), DistribuerJournalpostService {

    override fun distribuer(journalpostId: String, distribusjonstype: Distribusjonstype): DistribueringInternalResponse {
        backoff.forEachIndexed { index, delay ->
            try {
                return forsoekDistribuer(journalpostId, distribusjonstype)
            } catch (e: DokDistException) {
                logger.warn("Distribusjon av journalpost $journalpostId feilet (forsøk ${index + 1}), prøver igjen om ${delay.toMillis()} ms: ${e.message}", e)
            }
            Thread.sleep(delay.toMillis())
        }
        try {
            return forsoekDistribuer(journalpostId, distribusjonstype)
        } catch (e: DokDistException) {
            logger.error(e.message, e)
            throw e
        }
    }

    private fun forsoekDistribuer(journalpostId: String, distribusjonstype: Distribusjonstype): DistribueringInternalResponse {
        val response = try {
            restTemplate.exchange<DistribuerJournalpostResponse>(
                "$url/rest/v1/distribuerjournalpost",
                HttpMethod.POST,
                HttpEntity(
                    DistribuerJournalpostRequest(
                        journalpostId = journalpostId,
                        bestillendeFagsystem = BESTILLENDE_FAGSYSTEM,
                        dokumentProdApp = BESTILLENDE_FAGSYSTEM,
                        distribusjonstype = distribusjonstype,
                        distribusjonstidspunkt = Distribusjonstidspunkt.KJERNETID,
                    )
                )
            )
        } catch (e: HttpClientErrorException) {
            when (e.statusCode) {
                HttpStatus.CONFLICT -> {
                    logger.warn("$journalpostId er allerede distribuert.")
                    return DistribueringInternalResponse(null, Status.CONFLICT)
                }
                HttpStatus.GONE -> {
                    logger.warn("Mottaker av $journalpostId er død, så journalposten kan ikke distribueres.")
                    return DistribueringInternalResponse(null, Status.MOTTAKER_DOED)
                }
                else -> throw DokDistException("Kunne ikke distribuere journalpost $journalpostId (4xx): ${e.responseBodyAsString}", e)
            }
        } catch (e: HttpServerErrorException) {
            throw DokDistException("Kunne ikke distribuere journalpost $journalpostId (5xx): ${e.responseBodyAsString}", e)
        } catch (e: RestClientException) {
            throw DokDistException("Kunne ikke distribuere journalpost $journalpostId: ${e.message}", e)
        }

        return DistribueringInternalResponse(response.body ?: throw DokDistException("Fikk tomt svar fra distribuerjournalpost for journalpost: $journalpostId"), Status.OK)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(DistribuerJournalpostClient::class.java)
    }
}
