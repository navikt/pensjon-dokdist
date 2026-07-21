package no.nav.pensjon.dokdist

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@Order(2)
class WebSecurityConfig {

    private val logger = LoggerFactory.getLogger(WebSecurityConfig::class.java)

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http.authorizeHttpRequests { req ->
        req.requestMatchers("/api/internal/isAlive").permitAll()
        req.requestMatchers("/api/internal/isReady").permitAll()
        req.anyRequest().authenticated()
    }
        .oauth2Login { oauth2 ->
            oauth2.failureHandler { request, response, exception ->
                logger.error("OAuth2 login failed for request ${request.requestURI}", exception)
                response.sendRedirect("/login?error")
            }
        }
        .csrf { it.disable() }
        .cors { }
        .build()
}
