package no.nav.pensjon.dokdist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.getForObject
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
class HealthControllerTest(
    @LocalServerPort private val port: Int,
    @Autowired private val restTemplate: TestRestTemplate,
) {
    private val baseUrl = "http://localhost:$port/api/internal"

    @Test
    fun isAliveResponds() {
        assertThat(restTemplate.getForObject<String>("$baseUrl/isAlive")).contains("alive")
    }

    @Test
    fun isReadyResponds() {
        assertThat(restTemplate.getForObject<String>("$baseUrl/isReady")).contains("ready")
    }

}
