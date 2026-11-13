package no.nav.pensjon.dokdist.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import java.util.UUID

const val CALL_ID_HEADER = "Nav-Call-Id"
const val CALL_ID_MDC_KEY = "callId"

private val VALID_CALL_ID = Regex("^[A-Za-z0-9-]{1,64}$")

class CallIdFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val callId = (request as HttpServletRequest).getHeader(CALL_ID_HEADER)
            ?.takeIf { VALID_CALL_ID.matches(it) }
            ?: UUID.randomUUID().toString()

        (response as HttpServletResponse).setHeader(CALL_ID_HEADER, callId)
        MDC.put(CALL_ID_MDC_KEY, callId)
        try {
            chain.doFilter(request, response)
        } finally {
            MDC.remove(CALL_ID_MDC_KEY)
        }
    }
}
