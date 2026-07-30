package no.nav.pensjon.dokdist.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper

class TrailingSlashHandlerFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val path = httpRequest.requestURI

        if (path.endsWith("/")) {
            val newPath = path.substring(0, path.length - 1)
            val newRequest = CustomHttpServletRequestWrapper(httpRequest, newPath)
            chain.doFilter(newRequest, response)
        } else {
            chain.doFilter(request, response)
        }
    }

    private class CustomHttpServletRequestWrapper(
        request: HttpServletRequest,
        private val newPath: String,
    ) : HttpServletRequestWrapper(request) {

        override fun getRequestURI(): String = newPath

        override fun getRequestURL(): StringBuffer =
            StringBuffer("$scheme://$serverName:$serverPort$newPath")
    }
}
