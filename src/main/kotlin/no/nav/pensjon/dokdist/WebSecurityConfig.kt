package no.nav.pensjon.dokdist

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@Order(2)
class WebSecurityConfig {

    private val logger = LoggerFactory.getLogger(WebSecurityConfig::class.java)

    // Azure AD utsteder access-token med audience satt til vårt eget API (api://.../.default),
    // ikke Microsoft Graph. Standard OidcUserService kaller likevel Graphs userinfo-endepunkt
    // med dette tokenet, som feiler med 401 "Invalid audience". ID-token inneholder allerede
    // alle claims vi trenger, så vi hopper over userinfo-kallet.
    private fun oidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> =
        OAuth2UserService { userRequest ->
            DefaultOidcUser(listOf(SimpleGrantedAuthority("ROLE_USER")), userRequest.idToken)
        }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http.authorizeHttpRequests { req ->
        req.requestMatchers("/api/internal/isAlive").permitAll()
        req.requestMatchers("/api/internal/isReady").permitAll()
        req.anyRequest().authenticated()
    }
        .oauth2Login { oauth2 ->
            oauth2.userInfoEndpoint { it.oidcUserService(oidcUserService()) }
            oauth2.failureHandler { request, response, exception ->
                logger.error("OAuth2 login failed for request ${request.requestURI}", exception)
                response.sendRedirect("/login?error")
            }
        }
        .csrf { it.disable() }
        .cors { }
        .build()
}
