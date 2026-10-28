package no.nav.pensjon.dokdist

import org.springframework.boot.tomcat.TomcatConnectorCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TomcatConfig {
    @Bean
    fun maxLocaleCountCustomizer() = TomcatConnectorCustomizer { connector ->
        connector.maxLocaleCount = MAX_LOCALE_COUNT
    }

    companion object {
        const val MAX_LOCALE_COUNT = 500
    }
}
