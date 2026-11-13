package no.nav.pensjon.dokdist

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjon.dokdist.brevmetadata.BrevmetadataService
import no.nav.pensjon.dokdist.dokarkiv.DokarkivService
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostService
import no.nav.pensjon.dokdist.saf.SafService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"],
)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@Import(FeilmeldingIResponsTest.PermitAllSecurityConfig::class)
class FeilmeldingIResponsTest(
    @param:LocalServerPort private val port: Int,
    @param:Autowired private val restTemplate: TestRestTemplate,
) {
    @MockkBean
    private lateinit var saf: SafService

    @MockkBean
    private lateinit var brevmetadata: BrevmetadataService

    @MockkBean
    private lateinit var dokarkiv: DokarkivService

    @MockkBean
    private lateinit var distribuerJournalpost: DistribuerJournalpostService

    @TestConfiguration
    class PermitAllSecurityConfig {
        @Bean
        @Order(0)
        fun permitAllFilterChain(http: HttpSecurity): SecurityFilterChain =
            http
                .securityMatcher("/api/**", "/error")
                .csrf { it.disable() }
                .authorizeHttpRequests { it.anyRequest().permitAll() }
                .build()
    }

    @Test
    fun `feilrespons inneholder message fra exception`() {
        every { saf.fetchJournal("999") } returns null

        val response = restTemplate.getForEntity("http://localhost:$port/api/journalpost/999", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).contains("\"message\":\"Journalpost finnes ikke: 999\"")
    }
}
