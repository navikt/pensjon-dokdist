package no.nav.pensjon.dokdist.dokarkiv

import no.nav.pensjon.dokdist.auth.OnBehalfOfTokenResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.boot.restclient.test.MockServerRestTemplateCustomizer
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.server.ResponseStatusException

private const val endpoint = "http://dokarkiv.local"

class DokarkivClientTest {
    private val token = OnBehalfOfTokenResponse("Bearer", "their-scope", "1000", "the-token")
    private val journalpostId = "12345"
    private val journalfoerendeEnhet = "en-fin-enhet"
    private val ferdigstillUrl = "$endpoint/rest/journalpostapi/v1/journalpost/${journalpostId}/ferdigstill"

    private val mockServerCustomizer: MockServerRestTemplateCustomizer = MockServerRestTemplateCustomizer()
    private val dokarkiv = DokarkivClient(endpoint, token, RestTemplateBuilder(mockServerCustomizer))
    private val mockServer = mockServerCustomizer.server

    @Test
    fun `can ferdigstille journalpost`() {
        mockServer.expect(requestTo(ferdigstillUrl))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath(".journalfoerendeEnhet").value(journalfoerendeEnhet))
            .andRespond(withSuccess())

        dokarkiv.ferdigstillJournalpost(journalpostId, journalfoerendeEnhet)
        mockServer.verify()
    }

    @Test
    fun `handles badrequest`() {
        mockServer.expect(requestTo(ferdigstillUrl))
            .andRespond(withBadRequest())

        val exception = assertThrows<DokarkivException> {
            dokarkiv.ferdigstillJournalpost(journalpostId, journalfoerendeEnhet)
        }
        assertThat(exception).hasMessageContaining("Kunne ikke ferdigstille")

        mockServer.verify()
    }

    @Test
    fun `handles forbidden`() {
        mockServer.expect(requestTo(ferdigstillUrl))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        val exception = assertThrows<ResponseStatusException> {
            dokarkiv.ferdigstillJournalpost(journalpostId, journalfoerendeEnhet)
        }
        assertThat(exception).matches { it.statusCode == HttpStatus.FORBIDDEN }.hasMessageContaining("Ikke tilgang til")

        mockServer.verify()
    }

    @Test
    fun `handles other errors`() {
        mockServer.expect(requestTo(ferdigstillUrl))
            .andRespond(withServerError())

        val exception = assertThrows<DokarkivException> {
            dokarkiv.ferdigstillJournalpost(journalpostId, journalfoerendeEnhet)
        }
        assertThat(exception).hasMessageContaining("Ukjent feil")

        mockServer.verify()
    }
}
