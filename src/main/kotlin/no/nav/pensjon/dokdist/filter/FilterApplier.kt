package no.nav.pensjon.dokdist.filter

import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterApplier {

    @Bean
    fun trailingSlashFilter(): FilterRegistrationBean<Filter> {
        val registrationBean = FilterRegistrationBean<Filter>()
        registrationBean.filter = TrailingSlashHandlerFilter()
        registrationBean.addUrlPatterns("/*")
        return registrationBean
    }
}
