package no.nav.pensjon.dokdist

import org.springframework.web.bind.annotation.*

@RestController
class HealthController {
    @GetMapping("/api/internal/isReady")
    fun isReady(): String =
        "ready"

    @GetMapping("/api/internal/isAlive")
    fun isAlive(): String =
        "alive"
}
