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
        // Vi fikk veldig mange feil i prod med at requesten sendte med flere locales enn tomcat ville ta imot
        // Dette er en workaround for det. Ikke optimalt, men bedre enn feil.
        const val MAX_LOCALE_COUNT = 500
    }
}
