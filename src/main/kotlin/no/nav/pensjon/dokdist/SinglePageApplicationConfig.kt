package no.nav.pensjon.dokdist

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.*
import java.io.IOException
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest

@Configuration
class StaticContentConfig : WebMvcConfigurer {
    // Add resource handler for everything except /api
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("{_:^(?!api).*$}").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/site/**").addResourceLocations("classpath:/static/site/")
    }
}

@ControllerAdvice
class ForwardNonApi404ToIndex {

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNonApiRequest(request: HttpServletRequest): ResponseEntity<String> {
        return if (request.requestURI.startsWith("/api")) {
            ResponseEntity.notFound().build()
        } else {
            try {
                ClassPathResource("static/index.html").inputStream
                    .use { StreamUtils.copyToString(it, Charset.defaultCharset()) }
                    .let { ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(it) }
            } catch (ex: IOException) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't find index.html")
            }
        }
    }
}
