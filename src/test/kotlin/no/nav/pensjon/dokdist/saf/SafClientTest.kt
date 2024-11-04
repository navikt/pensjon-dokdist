package no.nav.pensjon.dokdist.saf

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.pensjon.dokdist.auth.OnBehalfOfTokenResponse
import no.nav.pensjon.dokdist.graphql.GraphQLResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.server.ResponseStatusException

private const val endpoint = "http://saf.local"

class SafClientTest {
    private val objectMapper = ObjectMapper()

    private val token = OnBehalfOfTokenResponse("Bearer", "their-scope", "1000", "the-token")
    private val journalpost = Journalpost("12345", "en-kul-enhet", listOf(Journalpost.Dokument("kult-brev")))
    private val responseJson = objectMapper.writeValueAsString(GraphQLResponse(mapOf("journalpost" to journalpost), null))
    private val graphqlUrl = "$endpoint/graphql"

    private val mockServerCustomizer: MockServerRestTemplateCustomizer = MockServerRestTemplateCustomizer()
    private val saf = SafClient(endpoint, token, RestTemplateBuilder(mockServerCustomizer))
    private val mockServer = mockServerCustomizer.server

    @Test
    fun `can fetch journalpost`() {
        mockServer.expect(requestTo(graphqlUrl))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath(".variables.journalpostId").value(journalpost.journalpostId))
            .andExpect(jsonPath(".query").exists())
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        saf.fetchJournal(journalpost.journalpostId)
        mockServer.verify()
    }

    @Test
    fun `missing journalpost with notfound status returns null`() {
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        assertThat(saf.fetchJournal(journalpost.journalpostId)).isNull()
        mockServer.verify()
    }

    @Test
    fun `missing journalpost with ok status returns null`() {
        val graphQLResponse = GraphQLResponse(mapOf("journalpost" to null), listOf(GraphQLResponse.Error("Fant ikke journalpost", null, null, null, null)))
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withSuccess(objectMapper.writeValueAsString(graphQLResponse), MediaType.APPLICATION_JSON))

        assertThat(saf.fetchJournal(journalpost.journalpostId)).isNull()
        mockServer.verify()
    }

    @Test
    fun `empty response fails`() {
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withSuccess())

        assertThrows<SafException> { saf.fetchJournal(journalpost.journalpostId) }
        mockServer.verify()
    }

    @Test
    fun `graphql error fails`() {
        val response = GraphQLResponse(null, listOf(GraphQLResponse.Error("Some syntax error or something", null, null, null, null)))
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        assertThrows<SafException> { saf.fetchJournal(journalpost.journalpostId) }
        mockServer.verify()
    }

    @Test
    fun `forbidden is rethrown as responseStatusException`() {
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        val exception = assertThrows<ResponseStatusException> { saf.fetchJournal(journalpost.journalpostId) }
        assertThat(exception).matches { it.statusCode == HttpStatus.FORBIDDEN }
        mockServer.verify()
    }

    @Test
    fun `bad request fails`() {
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withBadRequest())

        assertThrows<SafException> { saf.fetchJournal(journalpost.journalpostId) }
        mockServer.verify()
    }

    @Test
    fun `server error fails`() {
        mockServer.expect(requestTo(graphqlUrl))
            .andRespond(withServerError())

        assertThrows<SafException> { saf.fetchJournal(journalpost.journalpostId) }
        mockServer.verify()
    }

}
