package no.nav.pensjon.dokdist

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.*
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSecurityConfigTest(
    @param:LocalServerPort private val port: Int,
    @param:Autowired private var restTemplate: TestRestTemplate,
) {
    private val baseUrl = "http://localhost:$port"
    private val authRedirectUrl = "$baseUrl/oauth2/authorization/azure"

    init {
        restTemplate = restTemplate.withRedirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW)
    }

    @Test
    fun nonProtectedResponds() {
        assertThat(restTemplate.getForObject<String>("$baseUrl/api/internal/isAlive")).contains("alive")
        assertThat(restTemplate.getForObject<String>("$baseUrl/api/internal/isReady")).contains("ready")
    }

    @Test
    fun indexIsSecured() {
        assertThat(restTemplate.getForEntity<String>("$baseUrl/index.html")).isSecured()
    }

    @Test
    fun journalpostIsSecured() {
        assertThat(restTemplate.getForEntity<String>("$baseUrl/journalpost/12345")).isSecured()
    }

    @Test
    fun journalpostInfoIsSecured() {
        assertThat(restTemplate.getForEntity<String>("$baseUrl/api/journalpost/12345")).isSecured()
    }

    @Test
    fun distribuerJournalpostIsSecured() {
        assertThat(
            restTemplate.postForEntity<String>(
                "$baseUrl/api/journalpost/12314/send",
                DistribuerRequest("FS", null)
            )
        ).isSecured()
    }

    private fun <T : Any> ObjectAssert<ResponseEntity<T>>.isSecured(): ObjectAssert<ResponseEntity<T>> =
        matches({ it.statusCode == HttpStatus.FOUND }, "statusCode shoud be Found (302)")
            .matches({ it.headers[HttpHeaders.LOCATION]?.firstOrNull() == authRedirectUrl }, "Location header should be $authRedirectUrl")
}
