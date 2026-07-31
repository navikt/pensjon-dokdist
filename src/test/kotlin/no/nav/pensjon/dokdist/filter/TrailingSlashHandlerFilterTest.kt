package no.nav.pensjon.dokdist.filter

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TrailingSlashHandlerFilterTest {
    private val filter = TrailingSlashHandlerFilter()
    private val response = mockk<HttpServletResponse>()
    private val chain = mockk<FilterChain>(relaxed = true)

    @Test
    fun `strips trailing slash from request URI`() {
        val request = requestWithUri("/api/foo/")

        filter.doFilter(request, response, chain)

        val forwardedRequest = slot<HttpServletRequest>()
        verify { chain.doFilter(capture(forwardedRequest), response) }
        assertThat(forwardedRequest.captured.requestURI).isEqualTo("/api/foo")
    }

    @Test
    fun `rewrites request URL to match stripped URI`() {
        val request = requestWithUri("/api/foo/", scheme = "https", serverName = "dokdist.local", serverPort = 443)

        filter.doFilter(request, response, chain)

        val forwardedRequest = slot<HttpServletRequest>()
        verify { chain.doFilter(capture(forwardedRequest), response) }
        assertThat(forwardedRequest.captured.requestURL.toString())
            .isEqualTo("https://dokdist.local:443/api/foo")
    }

    @Test
    fun `leaves root path untouched`() {
        val request = requestWithUri("/")

        filter.doFilter(request, response, chain)

        verify { chain.doFilter(request, response) }
    }

    @Test
    fun `leaves path without trailing slash untouched`() {
        val request = requestWithUri("/api/foo")

        filter.doFilter(request, response, chain)

        verify { chain.doFilter(request, response) }
    }

    private fun requestWithUri(
        uri: String,
        scheme: String = "http",
        serverName: String = "localhost",
        serverPort: Int = 8080,
    ): HttpServletRequest {
        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns uri
        every { request.scheme } returns scheme
        every { request.serverName } returns serverName
        every { request.serverPort } returns serverPort
        return request
    }
}
