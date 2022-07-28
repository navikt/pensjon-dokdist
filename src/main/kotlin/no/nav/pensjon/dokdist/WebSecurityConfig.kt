package no.nav.pensjon.dokdist

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.*
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.web.servlet.invoke

@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/api/internal/isAlive", permitAll)
                authorize("/api/internal/isReady", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2Login { }
            csrf { disable() }
            cors { }
        }
        return http.build()
    }
}
