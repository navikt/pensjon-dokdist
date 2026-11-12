package no.nav.pensjon.dokdist.filter

import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.UUID

class CallIdFilterTest {
    private val filter = CallIdFilter()
    private val response = MockHttpServletResponse()
    private var callIdInChain: String? = null
    private val chain = FilterChain { _, _ -> callIdInChain = MDC.get(CALL_ID_MDC_KEY) }

    @Test
    fun `uses call id from request header`() {
        val request = MockHttpServletRequest().apply { addHeader(CALL_ID_HEADER, "abc-123") }

        filter.doFilter(request, response, chain)

        assertThat(callIdInChain).isEqualTo("abc-123")
        assertThat(response.getHeader(CALL_ID_HEADER)).isEqualTo("abc-123")
    }

    @Test
    fun `generates call id when header is missing`() {
        filter.doFilter(MockHttpServletRequest(), response, chain)

        assertThat(UUID.fromString(callIdInChain)).isNotNull
        assertThat(response.getHeader(CALL_ID_HEADER)).isEqualTo(callIdInChain)
    }

    @Test
    fun `replaces invalid call id`() {
        val request = MockHttpServletRequest().apply { addHeader(CALL_ID_HEADER, "abc\n123 <script>") }

        filter.doFilter(request, response, chain)

        assertThat(UUID.fromString(callIdInChain)).isNotNull
        assertThat(response.getHeader(CALL_ID_HEADER)).isEqualTo(callIdInChain)
    }

    @Test
    fun `clears MDC after request`() {
        filter.doFilter(MockHttpServletRequest(), response, chain)

        assertThat(MDC.get(CALL_ID_MDC_KEY)).isNull()
    }
}
