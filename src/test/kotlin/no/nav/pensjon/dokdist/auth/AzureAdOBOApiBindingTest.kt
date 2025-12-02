package no.nav.pensjon.dokdist.auth

import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.*


class AzureAdOBOApiBindingTest{
    private val mockServerCustomizer: MockServerRestTemplateCustomizer = MockServerRestTemplateCustomizer()
    private val token = OnBehalfOfTokenResponse(
        "bearer",
        "a-scope",
        "1000",
        "the-super-secret-access-token",
        null
    )
    private val apiClient = TestApiClient(token, RestTemplateBuilder(mockServerCustomizer))
    private val mockServer = mockServerCustomizer.server

    @Test
    fun `restTemplate has auth-header interceptor`() {
        val url = "http://something.local"
        mockServer.expect(requestTo(url))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer ${token.accessToken}"))
            .andRespond(withSuccess())

        apiClient.request(url)
        mockServer.verify()
    }

}

private class TestApiClient(accessToken: OnBehalfOfTokenResponse, restTemplateBuilder: RestTemplateBuilder) : AzureAdOBOApiBinding(accessToken, restTemplateBuilder) {
    fun request(url: String) {
        restTemplate.getForObject<String?>(url)
    }
}
