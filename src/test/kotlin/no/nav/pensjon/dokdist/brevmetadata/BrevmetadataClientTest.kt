package no.nav.pensjon.dokdist.brevmetadata

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import tools.jackson.databind.json.JsonMapper

private const val endpoint = "http://brevmetadata.local"

@RestClientTest(
    components = [BrevmetadataClient::class],
    properties = ["brevmetadata.url=$endpoint"]
)
class BrevmetadataClientTest(
    @Autowired private val brevmetadata: BrevmetadataClient,
    @Autowired private val mockRestServer: MockRestServiceServer,
    @Autowired private val objectMapper: JsonMapper,
) {
    private val brevdata = Brevdata(DokumentkategoriCode.IB)
    private val brevdataJson = objectMapper.writeValueAsString(brevdata)

    @Test
    fun `fetches brevdata`() {
        val kode = "crazy-letter"
        mockRestServer.expect(requestTo("/api/brevdata/brevForBrevkode/$kode"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(brevdataJson, MediaType.APPLICATION_JSON))

        brevmetadata.fetchBrevmetadata(kode)
    }

    @Test
    fun `fetch unknown brevkode returns null`() {
        val kode = "unknown-brevkode"
        mockRestServer.expect(requestTo("/api/brevdata/brevForBrevkode/$kode"))
            .andRespond(withBadRequest())

        assertNull(brevmetadata.fetchBrevmetadata(kode))
    }
    
    @Test
    fun `fetch handles empty response`() {
        val kode = "crazy-letter"
        mockRestServer.expect(requestTo("/api/brevdata/brevForBrevkode/$kode"))
            .andRespond(withSuccess("", MediaType.APPLICATION_JSON))

        assertNull(brevmetadata.fetchBrevmetadata(kode))
    }
}
