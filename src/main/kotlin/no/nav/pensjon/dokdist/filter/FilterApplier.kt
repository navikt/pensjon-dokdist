package no.nav.pensjon.dokdist.filter

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterApplier {

    @Bean
    fun trailingSlashFilter(): FilterRegistrationBean<TrailingSlashHandlerFilter> =
        FilterRegistrationBean(TrailingSlashHandlerFilter()).apply {
            addUrlPatterns("/*")
        }
}
