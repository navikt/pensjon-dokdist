package no.nav.pensjon.dokdist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.getForEntity
import org.springframework.boot.resttestclient.getForObject
import org.springframework.boot.test.context.*
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private const val INDEX_CONTENT = "Our test index"
private const val INDEX_JS_CONTENT = "var x = \"\""

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
@Import(value = [
    SinglePageApplicationConfigTest.DisableSecurityConfig::class,
    SinglePageApplicationConfigTest.TestController::class,
])
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
class SinglePageApplicationConfigTest(
    @LocalServerPort private val port: Int,
    @Autowired private val restTemplate: TestRestTemplate,
) {
    private val baseUrl = "http://localhost:$port"

    @TestConfiguration
    class DisableSecurityConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain = http.build()
    }

    @RestController
    class TestController {
        @GetMapping("/api/test")
        fun get(): ResponseEntity<String> = ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @Test
    fun indexHtmlResponds() {
        assertThat(restTemplate.getForObject<String>("$baseUrl/index.html")).contains(INDEX_CONTENT)
    }

    @Test
    fun otherExistingStaticFilesResponds() {
        assertThat(restTemplate.getForObject<String>("$baseUrl/index.js")).contains(INDEX_JS_CONTENT)
    }

    @Test
    fun `non existing static file responds with 404`() {
        assertThat(restTemplate.getForEntity<String>("$baseUrl/jadda.ts").statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun journalpostIsRedirectedToIndexHtml() {
        assertThat(restTemplate.getForObject<String>("$baseUrl/journalpost/123456")).contains(INDEX_CONTENT)
    }

    @Test
    fun handledRequestIsNotRedirected() {
        // Handled by TestController
        assertThat(restTemplate.getForEntity<String>("$baseUrl/api/test").statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `unhandled api requests responds with 404`() {
        assertThat(restTemplate.getForEntity<String>("$baseUrl/api/unhandled").statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}
