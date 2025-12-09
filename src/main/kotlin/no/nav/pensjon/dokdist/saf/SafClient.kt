package no.nav.pensjon.dokdist.saf

import com.fasterxml.jackson.databind.ObjectMapper
import net.logstash.logback.marker.RawJsonAppendingMarker
import no.nav.pensjon.dokdist.auth.*
import no.nav.pensjon.dokdist.graphql.*
import org.slf4j.LoggerFactory
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.client.HttpClientErrorException.Forbidden
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.client.RestClientException
import org.springframework.web.client.exchange
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets

interface SafService {
    fun fetchJournal(journalpostId: String): Journalpost?
}

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Feilet å hente journalpost i SAF")
class SafException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)

private data class FetchJournalVariables(val journalpostId: String)
private data class FetchJournalData(val journalpost: Journalpost?)


class SafClient(
    private val url: String,
    accessToken: OnBehalfOfTokenResponse,
    restTemplateBuilder: RestTemplateBuilder = RestTemplateBuilder(),
) : AzureAdOBOApiBinding(accessToken, restTemplateBuilder), SafService {
    private val query: String = ClassPathResource("saf/journalpostQuery.graphql").inputStream.use { StreamUtils.copyToString(it, StandardCharsets.UTF_8) }

    override fun fetchJournal(journalpostId: String): Journalpost? {
        return try {
            val response = restTemplate.exchange<GraphQLResponse<FetchJournalData>>(
                "$url/graphql",
                HttpMethod.POST,
                HttpEntity(
                    GraphQLRequest(query, FetchJournalVariables(journalpostId)),
                    HttpHeaders().apply {
                        contentType = MediaType.APPLICATION_JSON
                        add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    }
                ),
            ).body
            handleResponse(response, journalpostId)
        } catch (e: NotFound) {
            null
        } catch (e: Forbidden) {
            val msg = "Mangler tilgang til journalpost: $journalpostId"
            logger.info(
                RawJsonAppendingMarker("error_response", e.responseBodyAsString),
                msg,
                e
            )
            throw ResponseStatusException(HttpStatus.FORBIDDEN, msg)
        } catch (e: BadRequest) {
            val msg = "Feil i graphql-spørring til saf for journalpost: $journalpostId"
            logger.error(
                RawJsonAppendingMarker("error_response", e.responseBodyAsString),
                msg,
                e
            )
            throw SafException(msg, e)
        } catch (e: RestClientException) {
            val msg = "Kunne ikke hente journalpost for journalpostId $journalpostId: ukjent grunn"
            logger.error(msg, e)
            throw SafException(msg, e)
        }
    }

    private fun handleResponse(response: GraphQLResponse<FetchJournalData>?, journalpostId: String): Journalpost? {
        return if (response == null) {
            val msg = "Fikk tomt svar fra saf.journalpost for: $journalpostId"
            logger.error(msg)
            throw SafException(msg)
        } else if (response.data == null) {
            logger.error(
                RawJsonAppendingMarker(
                    "error_response", objectMapper.writeValueAsString(response.errors)
                ), "Journalpost GraphQL spørring feilet for: $journalpostId"
            )

            throw SafException("Hent journalpost feilet: $journalpostId")
        } else if (response.data.journalpost == null) {
            logger.warn("Journalpost mangler i svar fra SAF, men fikk ikke notFound som forventet.")
            null
        } else {
            response.data.journalpost
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SafClient::class.java)
        private val objectMapper = ObjectMapper()
    }
}
