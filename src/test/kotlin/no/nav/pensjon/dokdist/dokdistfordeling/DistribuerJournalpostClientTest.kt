package no.nav.pensjon.dokdist.dokdistfordeling

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.pensjon.dokdist.DokDistException
import no.nav.pensjon.dokdist.auth.OnBehalfOfTokenResponse
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostRequest.Distribusjonstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*

private const val endpoint = "http://dokdistfordeling.local"

class DistribuerJournalpostClientTest {
    private val objectMapper = ObjectMapper()

    private val token = OnBehalfOfTokenResponse("Bearer", "their-scope", "1000", "the-token")
    private val journalpostId = "12345"
    private val distribuerUrl = "$endpoint/rest/v1/distribuerjournalpost"
    private val response = DistribuerJournalpostResponse("991122")
    private val responseJson = objectMapper.writeValueAsString(response)

    private val mockServerCustomizer: MockServerRestTemplateCustomizer = MockServerRestTemplateCustomizer()
    private val distribuer = DistribuerJournalpostClient(endpoint, token, RestTemplateBuilder(mockServerCustomizer))
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
        mockServer.expect(requestTo(distribuerUrl))
            .andRespond(withSuccess())

        assertThrows<DokDistException> { distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK) }
        mockServer.verify()
    }

    @Test
    fun `handles badrequest`() {
        mockServer.expect(requestTo(distribuerUrl))
            .andRespond(withBadRequest())

        assertThrows<DokDistException> { distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK) }
        mockServer.verify()
    }

    @Test
    fun `handles server error`() {
        mockServer.expect(requestTo(distribuerUrl))
            .andRespond(withServerError())

        assertThrows<DokDistException> { distribuer.distribuer(journalpostId, Distribusjonstype.VEDTAK) }
        mockServer.verify()
    }

}
