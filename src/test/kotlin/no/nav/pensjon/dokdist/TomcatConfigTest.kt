package no.nav.pensjon.dokdist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.tomcat.TomcatWebServer
import org.springframework.boot.web.server.context.WebServerApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TomcatConfigTest {

    @Autowired
    private lateinit var context: WebServerApplicationContext

    @Test
    fun `connector har okt maxLocaleCount`() {
        val connector = (context.webServer as TomcatWebServer).tomcat.connector
        assertEquals(TomcatConfig.MAX_LOCALE_COUNT, connector.maxLocaleCount)
    }
}
