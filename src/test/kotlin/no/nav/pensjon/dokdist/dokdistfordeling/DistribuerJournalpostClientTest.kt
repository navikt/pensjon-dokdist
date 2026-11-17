package no.nav.pensjon.dokdist.dokdistfordeling

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.pensjon.dokdist.DokDistException
import no.nav.pensjon.dokdist.auth.OnBehalfOfTokenResponse
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostRequest.Distribusjonstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.restclient.test.MockServerRestTemplateCustomizer
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.ExpectedCount.times
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import java.io.IOException
import java.time.Duration

private const val endpoint = "http://dokdistfordeling.local"

class DistribuerJournalpostClientTest {
    private val objectMapper = ObjectMapper()

    private val token = OnBehalfOfTokenResponse("Bearer", "their-scope", "1000", "the-token")
    private val journalpostId = "12345"
    private val distribuerUrl = "$endpoint/rest/v1/distribuerjournalpost"
    private val response = DistribuerJournalpostResponse("991122")
    private val responseJson = objectMapper.writeValueAsString(response)

    private val mockServerCustomizer: MockServerRestTemplateCustomizer = MockServerRestTemplateCustomizer()
    private val distribuer = DistribuerJournalpostClient(endpoint, token, RestTemplateBuilder(mockServerCustomizer), List(3) { Duration.ZERO })
    private val mockServer = mockServerCustomizer.server

    @Test
    fun `can distribuere journalpost`() {
        val expectedRequest = DistribuerJournalpostRequest(
            journalpostId,
            BESTILLENDE_FAGSYSTEM,
            BESTILLENDE_FAGSYSTEM,
            Distribusjonstype.VEDTAK,
            DistribuerJournalpostRequest.Distribusjonstidspunkt.KJERNETID
        )
        mockServer.expect(requestTo(distribuerUrl))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(expectedRequest)))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        assertThat(distribuer.distribuer(journalpostId, expectedRequest.distribusjonstype).originalResponse).isEqualTo(response)
        mockServer.verify()
    }

    @Test
    fun `fails if response is empty`() {
        mockServer.expect(times(4), requestTo(distribuerUrl))
            .andRespond(withSuccess())

        assertThrows<DokDistException> { distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK) }
        mockServer.verify()
    }

    @Test
    fun `handles badrequest`() {
        mockServer.expect(times(4), requestTo(distribuerUrl))
            .andRespond(withBadRequest())

        assertThrows<DokDistException> { distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK) }
        mockServer.verify()
    }

    @Test
    fun `handles server error`() {
        mockServer.expect(times(4), requestTo(distribuerUrl))
            .andRespond(withServerError())

        assertThrows<DokDistException> { distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK) }
        mockServer.verify()
    }

    @Test
    fun `retries server error and succeeds`() {
        mockServer.expect(times(2), requestTo(distribuerUrl)).andRespond(withServerError())
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).originalResponse).isEqualTo(response)
        mockServer.verify()
    }

    @Test
    fun `retries badrequest and succeeds`() {
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withBadRequest())
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).originalResponse).isEqualTo(response)
        mockServer.verify()
    }

    @Test
    fun `retries empty response and succeeds`() {
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withSuccess())
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).originalResponse).isEqualTo(response)
        mockServer.verify()
    }

    @Test
    fun `retries network error and succeeds`() {
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withException(IOException("Connection reset")))
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).originalResponse).isEqualTo(response)
        mockServer.verify()
    }

    @Test
    fun `does not retry conflict`() {
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withStatus(HttpStatus.CONFLICT))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).status).isEqualTo(Status.CONFLICT)
        mockServer.verify()
    }

    @Test
    fun `does not retry gone`() {
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withStatus(HttpStatus.GONE))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).status).isEqualTo(Status.MOTTAKER_DOED)
        mockServer.verify()
    }

    @Test
    fun `reports conflict when retry hits already distributed`() {
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withServerError())
        mockServer.expect(once(), requestTo(distribuerUrl)).andRespond(withStatus(HttpStatus.CONFLICT))

        assertThat(distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK).status).isEqualTo(Status.CONFLICT)
        mockServer.verify()
    }
}
